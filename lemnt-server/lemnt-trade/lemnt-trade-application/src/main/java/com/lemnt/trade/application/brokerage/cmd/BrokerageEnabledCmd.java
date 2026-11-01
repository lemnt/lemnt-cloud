package com.lemnt.trade.application.brokerage.cmd;

import java.io.Serializable;

/** 管理端开通/取消分销资格命令。 */
public record BrokerageEnabledCmd(Long memberId, Boolean enabled) implements Serializable {
}
