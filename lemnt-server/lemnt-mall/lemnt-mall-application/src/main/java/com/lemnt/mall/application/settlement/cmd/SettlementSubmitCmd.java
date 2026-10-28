package com.lemnt.mall.application.settlement.cmd;

/** SettlementSubmitCmd 输入契约。 */
public record SettlementSubmitCmd(Long memberId,
        String settlementId,
        String requestNo) implements java.io.Serializable {}
