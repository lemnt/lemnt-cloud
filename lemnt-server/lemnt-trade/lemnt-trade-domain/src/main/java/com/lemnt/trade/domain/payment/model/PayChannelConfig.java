package com.lemnt.trade.domain.payment.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 支付渠道配置：同一应用下渠道编码唯一，config 为不透明 JSON 文本，不含真实渠道调用。 */
public record PayChannelConfig(
        Long id,
        Long appId,
        String code,
        Integer status,
        BigDecimal feeRate,
        String remark,
        String config,
        LocalDateTime createTime
) {
}
