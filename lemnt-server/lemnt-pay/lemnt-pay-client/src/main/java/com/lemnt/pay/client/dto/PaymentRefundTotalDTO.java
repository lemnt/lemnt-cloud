package com.lemnt.pay.client.dto;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 仅统计资金库已成功原路退款，不把售后计划计入。
 */
public record PaymentRefundTotalDTO(long refundCount, BigDecimal refundAmount)
    implements Serializable {
}
