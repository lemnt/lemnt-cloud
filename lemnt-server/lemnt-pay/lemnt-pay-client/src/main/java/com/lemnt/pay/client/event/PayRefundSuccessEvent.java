package com.lemnt.pay.client.event;

import java.io.Serializable;
import java.math.BigDecimal;

/** 现金原路退款事实；不直接改变任何订单或权益。 */
public record PayRefundSuccessEvent(
        String refundNo,
        String afterSaleNo,
        String paymentNo,
        String orderNo,
        Long memberId,
        String channel,
        BigDecimal amount,
        String channelRefundNo)
        implements Serializable {}
