package com.lemnt.order.application.settlement.cmd;

import java.io.Serializable;

/**
 * SettlementAbandonCmd 输入契约。
 */
public record SettlementAbandonCmd(Long memberId, String settlementId, String requestNo)
    implements Serializable {
}
