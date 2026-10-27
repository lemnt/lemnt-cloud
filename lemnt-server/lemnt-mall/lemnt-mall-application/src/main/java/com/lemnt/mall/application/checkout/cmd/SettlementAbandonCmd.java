package com.lemnt.mall.application.checkout.cmd;

/** SettlementAbandonCmd 输入契约。 */
public record SettlementAbandonCmd(Long memberId,
        String settlementId,
        String requestNo) implements java.io.Serializable {}
