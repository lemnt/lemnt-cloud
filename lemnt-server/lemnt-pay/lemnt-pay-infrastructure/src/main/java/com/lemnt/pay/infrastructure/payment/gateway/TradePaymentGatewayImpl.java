package com.lemnt.pay.infrastructure.payment.gateway;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.pay.domain.channel.model.PaymentChannel;
import com.lemnt.pay.domain.payment.gateway.TradePaymentGateway;
import com.lemnt.pay.domain.payment.model.PaymentFilter;
import com.lemnt.pay.domain.payment.model.PaymentOrder;
import com.lemnt.pay.domain.payment.model.PaymentRecord;
import com.lemnt.pay.domain.payment.model.VerifiedPaymentNotification;
import com.lemnt.pay.infrastructure.channel.support.PayChannelConfigResolver;
import com.lemnt.pay.infrastructure.payment.dataobject.PaymentOrderDO;
import com.lemnt.pay.infrastructure.payment.dataobject.TradePaymentDO;
import com.lemnt.pay.infrastructure.payment.mapper.PaymentOrderMapper;
import com.lemnt.pay.infrastructure.payment.mapper.TradePaymentMapper;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * 交易支付领域网关实现。
 */
@Component
@RequiredArgsConstructor
public class TradePaymentGatewayImpl implements TradePaymentGateway {
    private static final long WECHAT_ATTEMPT_SECONDS = 30 * 60L;

    private final TradePaymentMapper tradePaymentMapper;
    private final PaymentOrderMapper orderMapper;
    private final PayChannelConfigResolver channelConfigs;

    @Override
    public PaymentRecord lockByPaymentNo(String paymentNo) {
        return toRecord(tradePaymentMapper.lockByPaymentNo(paymentNo));
    }

    @Override
    public PaymentRecord lockForConfirmation(String orderNo, String paymentNo) {
        var attempt = tradePaymentMapper.findByPaymentNo(paymentNo);
        if (attempt == null || !orderNo.equals(attempt.getOrderNo())) return null;
        lockPaymentOrder(orderNo, attempt.getMemberId(), attempt.getAmount());
        return lockByPaymentNo(paymentNo);
    }

    @Override
    public PaymentOrder findPaymentOrder(String orderNo) {
        var order = orderMapper.selectById(orderNo);
        return order == null
                ? null
                : new PaymentOrder(
                        order.getPaymentOrderNo(),
                        order.getOrderNo(),
                        order.getMemberId(),
                        order.getAmount(),
                        order.getStatus(),
                        order.getPaidPaymentNo());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void closePaymentOrder(String orderNo) {
        var order = orderMapper.lockByOrderNo(orderNo);
        if (order != null && "WAITING".equals(order.getStatus())) {
            updateFunding(orderNo, "CLOSED", null);
        }
    }

    private PaymentOrderDO lockPaymentOrder(String orderNo, Long memberId, BigDecimal amount) {
        // 用唯一业务单号锁定主单；并发首次创建共用同一条原记录。
        orderMapper.createIfAbsent(
                new PaymentOrderDO()
                        .setOrderNo(orderNo)
                        .setPaymentOrderNo("PO" + UUID.randomUUID().toString().replace("-", ""))
                        .setMemberId(memberId)
                        .setAmount(amount));
        return orderMapper.lockByOrderNo(orderNo);
    }

    @Override
    public PageResult<PaymentRecord> page(PaymentFilter filter) {
        var page =
            tradePaymentMapper.selectPage(
                new Page<>(filter.pageNum(), filter.pageSize()),
                Wrappers.lambdaQuery(TradePaymentDO.class)
                    .eq(
                        filter.paymentNo() != null,
                        TradePaymentDO::getPaymentNo,
                        filter.paymentNo())
                    .eq(
                        filter.orderNo() != null,
                        TradePaymentDO::getOrderNo,
                        filter.orderNo())
                    .eq(
                        filter.channel() != null,
                        TradePaymentDO::getChannel,
                        filter.channel())
                    .eq(
                        filter.status() != null,
                        TradePaymentDO::getStatus,
                        filter.status())
                    .orderByDesc(TradePaymentDO::getId));
        return PageResult.of(
            page.getRecords().stream().map(this::toRecord).toList(), page.getTotal());
    }

    @Override
    public PaymentRecord findByPaymentNo(String paymentNo) {
        return toRecord(tradePaymentMapper.findByPaymentNo(paymentNo));
    }

    @Override
    public PaymentRecord findByOrderNoAndChannel(String orderNo, String channel) {
        return toRecord(tradePaymentMapper.findPreferredAttempt(orderNo, channel));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PaymentRecord createWaiting(
        String paymentNo,
        String orderNo,
        Long memberId,
        String channel,
        String appKey,
        BigDecimal amount) {
        var master = lockPaymentOrder(orderNo, memberId, amount);
        if (master.getAmount() == null
                || master.getAmount().compareTo(amount) != 0
                || (master.getMemberId() != null
                        && !Objects.equals(master.getMemberId(), memberId))) {
            throw new IllegalStateException("支付主单与原业务金额或归属不一致");
        }
        if (!"WAITING".equals(master.getStatus())) {
            throw new IllegalStateException("支付主单已结束或待对账");
        }
        var previous = tradePaymentMapper.findPreferredAttempt(orderNo, channel);
        if (previous != null) {
            verifyAttempt(previous, appKey, amount);
            if (!"CLOSED".equals(previous.getStatus())
                    || previous.getExpiresAtEpochSecond() == null
                    || appKey == null) {
                return toRecord(previous);
            }
        }
        boolean expiring = PaymentChannel.WECHAT.name().equals(channel) && appKey != null;
        TradePaymentDO row =
                new TradePaymentDO()
                        .setPaymentNo(paymentNo)
                        .setOrderNo(orderNo)
                        .setMemberId(memberId)
                        .setChannel(channel)
                        .setAppKey(appKey)
                        .setAmount(amount)
                        .setAttemptNo(previous == null ? 1 : previous.getAttemptNo() + 1)
                        .setExpiresAtEpochSecond(
                                expiring ? databaseEpochSecond() + WECHAT_ATTEMPT_SECONDS : null)
                        .setStatus("WAITING");
        tradePaymentMapper.createWaiting(row);
        var saved = tradePaymentMapper.findPreferredAttempt(orderNo, channel);
        verifyAttempt(saved, appKey, amount);
        return toRecord(saved);
    }

    private void verifyAttempt(TradePaymentDO row, String appKey, BigDecimal amount) {
        if (row == null
                || row.getAmount().compareTo(amount) != 0
                || (appKey != null && !appKey.equals(row.getAppKey()))) {
            throw new IllegalStateException("重复支付请求的金额或应用不一致");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean markSuccess(VerifiedPaymentNotification notification) {
        var attempt = tradePaymentMapper.findByPaymentNo(notification.paymentNo());
        if (attempt == null || !notification.orderNo().equals(attempt.getOrderNo())) return false;
        var master =
                lockPaymentOrder(attempt.getOrderNo(), attempt.getMemberId(), attempt.getAmount());
        var payment = toRecord(attempt);
        BigDecimal feeRate = feeRate(payment);
        BigDecimal fee =
                feeRate == null
                        ? null
                        : notification
                                .amount()
                                .multiply(feeRate)
                                .movePointLeft(2)
                                .setScale(2, RoundingMode.HALF_UP);
        boolean updated =
                tradePaymentMapper.markSuccess(
                                notification.paymentNo(),
                                notification.orderNo(),
                                notification.channel(),
                                notification.amount(),
                                notification.channelTradeNo(),
                                notification.notifyId(),
                                feeRate,
                                fee)
                        == 1;
        if (updated && !"RECONCILE_REQUIRED".equals(master.getStatus())) {
            boolean conflict =
                    !"WAITING".equals(master.getStatus())
                            || master.getAmount() == null
                            || master.getAmount().compareTo(payment.amount()) != 0
                            || (master.getMemberId() != null
                                    && attempt.getMemberId() != null
                                    && !master.getMemberId().equals(attempt.getMemberId()));
            // 核实的渠道收款仍保存。异常主单保留原成功尝试供对账。
            updateFunding(
                    payment.orderNo(),
                    conflict ? "RECONCILE_REQUIRED" : "SUCCESS",
                    conflict ? master.getPaidPaymentNo() : payment.paymentNo());
        }
        return updated;
    }

    private void updateFunding(String orderNo, String status, String paidPaymentNo) {
        if (orderMapper.updateFunding(orderNo, status, paidPaymentNo) != 1) {
            throw new IllegalStateException("支付主单状态写入失败");
        }
    }

    /** 钱包为内部扣款。其他渠道只按原应用下已知费率保存，缺失配置留空。 */
    private BigDecimal feeRate(PaymentRecord payment) {
        if (PaymentChannel.WALLET.name().equals(payment.channel())) return BigDecimal.ZERO;
        var config = channelConfigs.resolve(payment.appKey(), payment.channel());
        return config == null ? null : config.feeRate();
    }

    @Override
    public List<PaymentRecord> findByOrderNo(String orderNo) {
        return tradePaymentMapper
            .selectList(
                Wrappers.lambdaQuery(TradePaymentDO.class)
                    .eq(TradePaymentDO::getOrderNo, orderNo)
                    .orderByAsc(TradePaymentDO::getId))
            .stream()
            .map(this::toRecord)
            .toList();
    }

    @Override
    public boolean markClosed(String paymentNo) {
        return tradePaymentMapper.markClosed(paymentNo) == 1;
    }

    @Override
    public List<PaymentRecord> waitingForQuery(long afterId, int limit) {
        return tradePaymentMapper
                .selectList(
                        Wrappers.lambdaQuery(TradePaymentDO.class)
                                .gt(TradePaymentDO::getId, afterId)
                                .eq(TradePaymentDO::getStatus, "WAITING")
                                .eq(TradePaymentDO::getChannel, "WECHAT")
                                .isNotNull(TradePaymentDO::getAppKey)
                                .and(
                                        deadline ->
                                                deadline.isNull(
                                                                TradePaymentDO
                                                                        ::getExpiresAtEpochSecond)
                                                        .or()
                                                        .gt(
                                                                TradePaymentDO
                                                                        ::getExpiresAtEpochSecond,
                                                                databaseEpochSecond()))
                                .orderByAsc(TradePaymentDO::getId)
                                .last("LIMIT " + limit))
                .stream()
                .map(this::toRecord)
                .toList();
    }

    @Override
    public long databaseEpochSecond() {
        return tradePaymentMapper.databaseEpochSecond();
    }

    @Override
    public List<PaymentRecord> expiredWaiting(long afterId, int limit) {
        return tradePaymentMapper
                .selectList(
                        Wrappers.lambdaQuery(TradePaymentDO.class)
                                .gt(TradePaymentDO::getId, afterId)
                                .eq(TradePaymentDO::getStatus, "WAITING")
                                .eq(TradePaymentDO::getChannel, "WECHAT")
                                .le(TradePaymentDO::getExpiresAtEpochSecond, databaseEpochSecond())
                                .orderByAsc(TradePaymentDO::getId)
                                .last("LIMIT " + limit))
                .stream()
                .map(this::toRecord)
                .toList();
    }

    private PaymentRecord toRecord(TradePaymentDO row) {
        return row == null
                ? null
                : new PaymentRecord(
                        row.getId(),
                        row.getPaymentNo(),
                        row.getOrderNo(),
                        row.getChannel(),
                        row.getAppKey(),
                        row.getChannelTradeNo(),
                        row.getAmount(),
                        row.getStatus(),
                        row.getNotifyId(),
                        row.getChannelFeeRate(),
                        row.getChannelFeeAmount(),
                        row.getExpiresAtEpochSecond());
    }
}
