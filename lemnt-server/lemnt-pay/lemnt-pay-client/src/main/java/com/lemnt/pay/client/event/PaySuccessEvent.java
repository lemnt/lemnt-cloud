package com.lemnt.pay.client.event;

import java.io.Serializable;
import java.math.BigDecimal;

/** 渠道已核验的现金支付事实，订单状态及权益由order决定。 */
public record PaySuccessEvent(
        String paymentNo,
        String orderNo,
        Long memberId,
        String channel,
        BigDecimal amount,
        String channelTradeNo)
        implements Serializable {}
