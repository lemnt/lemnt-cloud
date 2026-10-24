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

@Component
@RequiredArgsConstructor
public class TradePaymentGatewayImpl implements TradePaymentGateway {
    private final TradePaymentMapper mapper;

    @Override
    public PaymentRecord findByPaymentNo(String paymentNo) {
        TradePaymentDO row = mapper.selectOne(Wrappers.lambdaQuery(TradePaymentDO.class)
                .eq(TradePaymentDO::getPaymentNo, paymentNo).last("LIMIT 1"));
        return toRecord(row);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PaymentRecord createWaiting(String paymentNo, String orderNo, String channel,
                                       java.math.BigDecimal amount) {
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

    private PaymentRecord toRecord(TradePaymentDO row) {
        return row == null ? null : new PaymentRecord(row.getId(), row.getPaymentNo(), row.getOrderNo(),
                row.getChannel(), row.getChannelTradeNo(), row.getAmount(), row.getStatus(), row.getNotifyId());
    }
}