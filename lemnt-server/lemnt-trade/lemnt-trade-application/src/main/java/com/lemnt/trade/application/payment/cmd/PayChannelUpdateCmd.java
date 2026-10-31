package com.lemnt.trade.application.payment.cmd;

import java.io.Serializable;
import java.math.BigDecimal;

/** 修改支付渠道配置，所属应用与渠道编码不可修改。 */
public record PayChannelUpdateCmd(
        Long id,
        Integer status,
        BigDecimal feeRate,
        String remark,
        String config
) implements Serializable {
}
