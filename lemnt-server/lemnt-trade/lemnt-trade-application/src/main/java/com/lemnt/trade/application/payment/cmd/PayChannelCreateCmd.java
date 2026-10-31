package com.lemnt.trade.application.payment.cmd;

import java.io.Serializable;
import java.math.BigDecimal;

/** 新增支付渠道配置。 */
public record PayChannelCreateCmd(
        Long appId,
        String code,
        Integer status,
        BigDecimal feeRate,
        String remark,
        String config
) implements Serializable {
}
