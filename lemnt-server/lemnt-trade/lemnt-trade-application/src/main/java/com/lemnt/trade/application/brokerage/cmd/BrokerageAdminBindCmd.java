package com.lemnt.trade.application.brokerage.cmd;

import java.io.Serializable;

/** 管理端修改推广员命令。 */
public record BrokerageAdminBindCmd(Long memberId, Long bindUserId) implements Serializable {}
