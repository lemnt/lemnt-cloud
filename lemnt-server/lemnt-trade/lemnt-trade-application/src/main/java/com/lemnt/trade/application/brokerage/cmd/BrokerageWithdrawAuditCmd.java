package com.lemnt.trade.application.brokerage.cmd;

import java.io.Serializable;

/** 管理端审核提现命令。 */
public record BrokerageWithdrawAuditCmd(Long id, Boolean approved, String reason, Long operatorId)
        implements Serializable {}
