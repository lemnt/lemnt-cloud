package com.lemnt.pay.application.payment.dto;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 支付单管理查询结果。
 */
public record PaymentDTO(
        Long id,
        String paymentNo,
        String orderNo,
        String channel,
        String channelTradeNo,
        BigDecimal amount,
        String status,
        BigDecimal channelFeeRate,
        BigDecimal channelFeeAmount,
        String paymentOrderNo,
        String paymentOrderStatus)
        implements Serializable {
    /** 兼容未提供渠道费率的结果。 */
    public PaymentDTO(
            Long id,
            String paymentNo,
            String orderNo,
            String channel,
            String channelTradeNo,
            BigDecimal amount,
            String status) {
        this(
                id,
                paymentNo,
                orderNo,
                channel,
                channelTradeNo,
                amount,
                status,
                null,
                null,
                null,
                null);
    }

    /** 历史明细和分页可只提供渠道记录与费率。 */
    public PaymentDTO(
            Long id,
            String paymentNo,
            String orderNo,
            String channel,
            String channelTradeNo,
            BigDecimal amount,
            String status,
            BigDecimal channelFeeRate,
            BigDecimal channelFeeAmount) {
        this(
                id,
                paymentNo,
                orderNo,
                channel,
                channelTradeNo,
                amount,
                status,
                channelFeeRate,
                channelFeeAmount,
                null,
                null);
    }
}
