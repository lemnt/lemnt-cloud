package com.lemnt.trade.infrastructure.payment.gateway;

import com.lemnt.trade.domain.payment.gateway.TradePaymentGateway;
import com.lemnt.trade.domain.payment.model.PaymentRecord;
import com.lemnt.trade.domain.payment.model.VerifiedPaymentNotification;
import com.lemnt.trade.infrastructure.payment.dataobject.TradePaymentDO;
import com.lemnt.trade.infrastructure.payment.mapper.TradePaymentMapper;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 交易支付领域网关实现。
 */
@Component
@RequiredArgsConstructor
public class TradePaymentGatewayImpl implements TradePaymentGateway {
    private final TradePaymentMapper tradePaymentMapper;

    @Override
    public PaymentRecord findByPaymentNo(String paymentNo) {
        return toRecord(tradePaymentMapper.selectOne(Wrappers.lambdaQuery(TradePaymentDO.class)
                .eq(TradePaymentDO::getPaymentNo, paymentNo).last("LIMIT 1")));
    }

    @Override
    public PaymentRecord findByOrderNoAndChannel(String orderNo, String channel) {
        return toRecord(tradePaymentMapper.selectOne(Wrappers.lambdaQuery(TradePaymentDO.class)
                .eq(TradePaymentDO::getOrderNo, orderNo).eq(TradePaymentDO::getChannel, channel)
                .last("LIMIT 1")));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PaymentRecord createWaiting(String paymentNo, String orderNo, String channel, BigDecimal amount) {
        TradePaymentDO row = new TradePaymentDO().setPaymentNo(paymentNo).setOrderNo(orderNo)
                .setChannel(channel).setAmount(amount).setStatus("WAITING");
        tradePaymentMapper.insert(row);
        return toRecord(row);
    }

    @Override
    public boolean markSuccess(VerifiedPaymentNotification notification) {
        return tradePaymentMapper.markSuccess(notification.paymentNo(), notification.orderNo(), notification.channel(),
                notification.amount(), notification.channelTradeNo(), notification.notifyId()) == 1;
    }

    @Override
    public List<PaymentRecord> findByOrderNo(String orderNo) {
        return tradePaymentMapper.selectList(Wrappers.lambdaQuery(TradePaymentDO.class)
                .eq(TradePaymentDO::getOrderNo, orderNo).orderByAsc(TradePaymentDO::getId))
                .stream().map(this::toRecord).toList();
    }

    @Override
    public boolean markClosed(String paymentNo) {
        return tradePaymentMapper.markClosed(paymentNo) == 1;
    }

    private PaymentRecord toRecord(TradePaymentDO row) {
        return row == null ? null : new PaymentRecord(row.getId(), row.getPaymentNo(), row.getOrderNo(),
                row.getChannel(), row.getChannelTradeNo(), row.getAmount(), row.getStatus(), row.getNotifyId());
    }
}
