package com.lemnt.order.application.brokerage.cmd;

import java.io.Serializable;

/**
 * 会员绑定推广员命令。
 */
public record BrokerageBindCmd(Long memberId, Long bindUserId) implements Serializable {
}
