package com.lemnt.trade.infrastructure.payment.gateway;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.trade.domain.payment.gateway.PayNotifyTaskGateway;
import com.lemnt.trade.domain.payment.model.PayNotifyLog;
import com.lemnt.trade.domain.payment.model.PayNotifyTask;
import com.lemnt.trade.domain.payment.model.PayNotifyTaskFilter;
import com.lemnt.trade.infrastructure.payment.converter.PayNotifyTaskConverter;
import com.lemnt.trade.infrastructure.payment.dataobject.PayNotifyLogDO;
import com.lemnt.trade.infrastructure.payment.dataobject.PayNotifyTaskDO;
import com.lemnt.trade.infrastructure.payment.mapper.PayNotifyLogMapper;
import com.lemnt.trade.infrastructure.payment.mapper.PayNotifyTaskMapper;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/** trade_pay_notify_task/log 持久化实现。 */
@Component
@RequiredArgsConstructor
public class PayNotifyTaskGatewayImpl implements PayNotifyTaskGateway {
    private final PayNotifyTaskMapper tasks;
    private final PayNotifyLogMapper logs;

    @Override
    public PayNotifyTask createIfAbsent(PayNotifyTask task) {
        try {
            var row = PayNotifyTaskConverter.toDO(task);
            tasks.insert(row);
            return PayNotifyTaskConverter.toTask(row);
        } catch (DuplicateKeyException duplicate) {
            return findByBiz(task.type(), task.bizNo());
        }
    }

    @Override
    public PayNotifyTask find(Long id) {
        return PayNotifyTaskConverter.toTask(tasks.selectById(id));
    }

    @Override
    public PayNotifyTask findByBiz(String type, String bizNo) {
        return PayNotifyTaskConverter.toTask(tasks.selectOne(Wrappers.<PayNotifyTaskDO>lambdaQuery()
                .eq(PayNotifyTaskDO::getType, type).eq(PayNotifyTaskDO::getBizNo, bizNo).last("LIMIT 1")));
    }

    @Override
    public boolean claim(Long id, int expectedTimes) {
        return tasks.claim(id, expectedTimes, databaseNow()) == 1;
    }

    @Override
    public boolean finishSuccess(Long id, int attemptNo, String response, LocalDateTime now) {
        return tasks.finishSuccess(id, attemptNo, response, now) == 1;
    }

    @Override
    public boolean finishFailed(Long id, int attemptNo, String response, LocalDateTime nextTime,
            LocalDateTime now) {
        return tasks.finishFailed(id, attemptNo, response, nextTime, now) == 1;
    }

    @Override
    public void appendLog(PayNotifyLog log) {
        logs.insert(PayNotifyTaskConverter.toDO(log));
    }

    @Override
    public List<PayNotifyLog> logs(Long taskId) {
        return logs.selectList(Wrappers.<PayNotifyLogDO>lambdaQuery()
                        .eq(PayNotifyLogDO::getTaskId, taskId)
                        .orderByAsc(PayNotifyLogDO::getNotifyTimes).orderByAsc(PayNotifyLogDO::getId))
                .stream().map(PayNotifyTaskConverter::toLog).toList();
    }

    @Override
    public PageResult<PayNotifyTask> page(PayNotifyTaskFilter filter) {
        var page = tasks.selectPage(new Page<PayNotifyTaskDO>(filter.pageNum(), filter.pageSize()),
                Wrappers.<PayNotifyTaskDO>lambdaQuery()
                        .eq(filter.type() != null, PayNotifyTaskDO::getType, filter.type())
                        .eq(filter.bizNo() != null, PayNotifyTaskDO::getBizNo, filter.bizNo())
                        .eq(filter.orderNo() != null, PayNotifyTaskDO::getOrderNo, filter.orderNo())
                        .eq(filter.status() != null, PayNotifyTaskDO::getStatus, filter.status())
                        .orderByDesc(PayNotifyTaskDO::getId));
        return PageResult.of(page.getRecords().stream().map(PayNotifyTaskConverter::toTask).toList(),
                page.getTotal());
    }

    @Override
    public List<PayNotifyTask> findDue(LocalDateTime now, int limit) {
        return tasks.findDue(now, limit).stream().map(PayNotifyTaskConverter::toTask).toList();
    }

    @Override
    public boolean resetForRetry(Long id, LocalDateTime now) {
        return tasks.resetForRetry(id, now) == 1;
    }

    @Override
    public LocalDateTime databaseNow() {
        return tasks.databaseNow();
    }
}
