package com.lemnt.pay.application.payment.service;

import com.lemnt.pay.domain.payment.gateway.PayNotifyTaskGateway;
import com.lemnt.pay.domain.payment.model.PayNotifyLog;
import com.lemnt.pay.domain.payment.model.PayNotifyTask;
import com.lemnt.pay.domain.payment.model.PayNotifyTaskStatus;
import com.lemnt.pay.domain.payment.model.PayNotifyType;
import com.lemnt.pay.domain.payment.model.PaymentRecord;
import com.lemnt.pay.domain.paymentrefund.model.PaymentRefund;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * 支付结果下游派发：持久化任务、立即尝试、失败按退避重试。
 */
@Component
@RequiredArgsConstructor
public class PayNotifyTaskService {
    private static final int[] FREQUENCY_SECONDS = {15, 15, 30, 180, 1800, 1800, 1800, 3600};
    public static final int MAX_NOTIFY_TIMES = 8;
    private static final int RESPONSE_MAX = 500;

    private final PayNotifyTaskGateway tasks;
    private final PayNotifyDispatcher dispatcher;
    private final ApplicationEventPublisher publisher;

    /**
     * 幂等入队，同一 type+bizNo 返回既有任务。
     */
    public PayNotifyTask enqueue(String type, String bizNo, String orderNo) {
        if (Arrays.stream(PayNotifyType.values()).noneMatch(value -> value.name().equals(type))) {
            throw new IllegalArgumentException("派发类型不合法");
        }
        require(bizNo, 40, "业务单号");
        require(orderNo, 40, "订单编号");
        return tasks.createIfAbsent(
            new PayNotifyTask(
                null,
                type,
                bizNo,
                orderNo,
                PayNotifyTaskStatus.PENDING.name(),
                0,
                MAX_NOTIFY_TIMES,
                null,
                null,
                null,
                null));
    }

    /** 在资金事务中登记任务，提交后唤醒异步派发，到期扫描负责补偿。 */
    public void enqueueForDelivery(PayNotifyType type, String bizNo, String orderNo) {
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            throw new IllegalStateException("支付通知任务需要资金事务");
        }
        var task = enqueue(type.name(), bizNo, orderNo);
        if (!orderNo.equals(task.orderNo())) {
            throw new IllegalStateException("原支付通知任务的订单归属不一致");
        }
        if (PayNotifyTaskStatus.PENDING.name().equals(task.status())) {
            publisher.publishEvent(new NotifyReady(task.id()));
        }
    }

    /** 原支付快照设置地址时，独立登记应用支付成功通知。 */
    public void enqueueAppPayment(PaymentRecord payment) {
        if (payment.notifyUrl() != null) {
            enqueueForDelivery(PayNotifyType.PAY_HTTP, payment.paymentNo(), payment.orderNo());
        }
    }

    /** 退款成功和渠道明确失败分别去重，同编号后续重试成功仍可通知。 */
    public void enqueueAppRefund(PaymentRefund refund) {
        if (refund.notifyUrl() == null) return;
        if ("SUCCESS".equals(refund.status())) {
            enqueueForDelivery(PayNotifyType.REFUND_HTTP, refund.refundNo(), refund.orderNo());
        } else if ("FAILED".equals(refund.status())) {
            enqueueForDelivery(PayNotifyType.REFUND_FAIL_HTTP, refund.refundNo(), refund.orderNo());
        }
    }

    @Async
    @TransactionalEventListener
    public void deliverAfterCommit(NotifyReady event) {
        attempt(event.taskId());
    }

    public record NotifyReady(Long taskId) {}

    /**
     * 抢占并派发一次；已成功返回 true，失败记录日志与下次时间后返回 false。
     */
    public boolean attempt(Long id) {
        var task = tasks.find(id);
        if (task == null) {
            return false;
        }
        if (PayNotifyTaskStatus.SUCCESS.name().equals(task.status())) {
            return true;
        }
        if (task.notifyTimes() >= task.maxNotifyTimes()) {
            return false;
        }
        var startedAt = tasks.databaseNow();
        // 当前RPC超时5秒，整任务包含多个下游步骤，租约预留120秒；业务端仍必须幂等。
        boolean claimed =
            task.executeVersion() == null
                ? tasks.claim(id, task.notifyTimes())
                : tasks.claimVersion(
                id,
                task.notifyTimes(),
                task.executeVersion(),
                startedAt.plusSeconds(120));
        if (!claimed) {
            // 被其他执行者抢占或状态已变化，按最新状态判断是否已成功。
            var current = tasks.find(id);
            return current != null && PayNotifyTaskStatus.SUCCESS.name().equals(current.status());
        }
        int attemptNo = task.notifyTimes() + 1;
        var now = tasks.databaseNow();
        try {
            dispatcher.dispatch(task);
            boolean finished =
                task.executeVersion() == null
                    ? tasks.finishSuccess(id, attemptNo, "OK", now)
                    : tasks.finishVersion(
                    id,
                    task.executeVersion() + 1,
                    true,
                    "OK",
                    null,
                    tasks.databaseNow());
            if (!finished) {
                return false;
            }
            log(id, attemptNo, PayNotifyTaskStatus.SUCCESS.name(), "OK");
            return true;
        } catch (RuntimeException error) {
            String message = message(error);
            if (task.executeVersion() == null) {
                tasks.finishFailed(id, attemptNo, message, now.plusSeconds(delay(attemptNo)), now);
            } else {
                var endedAt = tasks.databaseNow();
                tasks.finishVersion(
                    id,
                    task.executeVersion() + 1,
                    false,
                    message,
                    endedAt.plusSeconds(delay(attemptNo)),
                    endedAt);
            }
            log(id, attemptNo, PayNotifyTaskStatus.FAILED.name(), message);
            return false;
        }
    }

    /**
     * 调度器批量重试到期任务，单条失败不影响其余任务。
     */
    public int processDue(int limit) {
        var now = tasks.databaseNow();
        int processed = 0;
        for (var task : tasks.findDue(now, limit)) {
            try {
                attempt(task.id());
            } catch (RuntimeException ignored) {
                // attempt 内部已记录失败；继续处理下一条。
            }
            processed++;
        }
        return processed;
    }

    /**
     * 人工重发：重置次数后立即尝试，成功任务拒绝重置。
     */
    public boolean retry(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("任务编号不合法");
        }
        var task = tasks.find(id);
        if (task == null) {
            throw new IllegalArgumentException("派发任务不存在");
        }
        if (PayNotifyTaskStatus.SUCCESS.name().equals(task.status())) {
            throw new IllegalStateException("任务已成功，无需重发");
        }
        if (!tasks.resetForRetry(id, tasks.databaseNow())) {
            return false;
        }
        return attempt(id);
    }

    private void log(Long taskId, int attemptNo, String status, String response) {
        try {
            tasks.appendLog(new PayNotifyLog(null, taskId, attemptNo, status, response, null));
        } catch (RuntimeException ignored) {
            // 日志失败不回滚任务状态，任务状态以任务表为准。
        }
    }

    private int delay(int attemptNo) {
        return FREQUENCY_SECONDS[Math.min(attemptNo - 1, FREQUENCY_SECONDS.length - 1)];
    }

    private String message(RuntimeException error) {
        String value = error.getMessage();
        if (value == null || value.isBlank()) {
            value = error.getClass().getSimpleName();
        }
        return value.length() > RESPONSE_MAX ? value.substring(0, RESPONSE_MAX) : value;
    }

    private void require(String value, int max, String field) {
        if (value == null || value.isBlank() || value.length() > max) {
            throw new IllegalArgumentException(field + "不合法");
        }
    }
}
