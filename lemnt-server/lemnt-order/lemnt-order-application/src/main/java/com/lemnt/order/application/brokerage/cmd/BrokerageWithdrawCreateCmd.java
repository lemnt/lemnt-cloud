package com.lemnt.order.application.brokerage.cmd;

import java.io.Serializable;
import java.math.BigDecimal;

/** 会员申请佣金提现命令。 */
public record BrokerageWithdrawCreateCmd(
        Long memberId,
        BigDecimal price,
        Integer type,
        String userName,
        String userAccount,
        String qrCodeUrl,
        String bankName,
        String bankAddress)
        implements Serializable {}
