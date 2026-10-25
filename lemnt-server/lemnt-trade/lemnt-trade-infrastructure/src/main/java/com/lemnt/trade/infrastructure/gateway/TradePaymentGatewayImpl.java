package com.lemnt.trade.infrastructure.gateway;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.lemnt.trade.domain.gateway.TradePaymentGateway;
import com.lemnt.trade.domain.model.PaymentRecord;
import com.lemnt.trade.domain.model.VerifiedPaymentNotification;
import com.lemnt.trade.infrastructure.dataobject.TradePaymentDO;
import com.lemnt.trade.infrastructure.mapper.TradePaymentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class TradePaymentGatewayImpl implements TradePaymentGateway {
    private final TradePaymentMapper mapper;

    @Override
    public PaymentRecord findByPaymentNo(String paymentNo) {
        return toRecord(mapper.selectOne(Wrappers.lambdaQuery(TradePaymentDO.class)
                .eq(TradePaymentDO::getPaymentNo, paymentNo).last("LIMIT 1")));
    }

    @Override
    public PaymentRecord findByOrderNoAndChannel(String orderNo, String channel) {
        return toRecord(mapper.selectOne(Wrappers.lambdaQuery(TradePaymentDO.class)
                .eq(TradePaymentDO::getOrderNo, orderNo).eq(TradePaymentDO::getChannel, channel)
                .last("LIMIT 1")));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PaymentRecord createWaiting(String paymentNo, String orderNo, String channel, BigDecimal amount) {
        TradePaymentDO row = new TradePaymentDO().setPaymentNo(paymentNo).setOrderNo(orderNo)
                .setChannel(channel).setAmount(amount).setStatus("WAITING");
        mapper.insert(row);
        return toRecord(row);
    }

    @Override
    public boolean markSuccess(VerifiedPaymentNotification notification) {
        return mapper.markSuccess(notification.paymentNo(), notification.orderNo(), notification.channel(),
                notification.amount(), notification.channelTradeNo(), notification.notifyId()) == 1;
    }

    @Override
    public java.util.List<PaymentRecord> findByOrderNo(String orderNo) {
        return mapper.selectList(Wrappers.lambdaQuery(TradePaymentDO.class)
                .eq(TradePaymentDO::getOrderNo, orderNo).orderByAsc(TradePaymentDO::getId))
                .stream().map(this::toRecord).toList();
    }

    @Override
    public boolean markClosed(String paymentNo) {
        return mapper.markClosed(paymentNo) == 1;
    }

    private PaymentRecord toRecord(TradePaymentDO row) {
        return row == null ? null : new PaymentRecord(row.getId(), row.getPaymentNo(), row.getOrderNo(),
                row.getChannel(), row.getChannelTradeNo(), row.getAmount(), row.getStatus(), row.getNotifyId());
    }
}