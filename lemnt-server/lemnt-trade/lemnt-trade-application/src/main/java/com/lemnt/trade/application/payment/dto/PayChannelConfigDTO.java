package com.lemnt.trade.application.payment.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 支付渠道配置应用输出。 */
public record PayChannelConfigDTO(
        Long id,
        Long appId,
        String code,
        Integer status,
        BigDecimal feeRate,
        String remark,
        String config,
        LocalDateTime createTime
) implements Serializable {
}
