package com.lemnt.order.application.settlement.cmd;

import java.io.Serializable;

/** SettlementSubmitCmd 输入契约。 */
public record SettlementSubmitCmd(Long memberId, String settlementId, String requestNo)
        implements Serializable {}
