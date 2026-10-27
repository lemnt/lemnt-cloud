package com.lemnt.mall.application.checkout.cmd;

/** SettlementCreateCmd 输入契约。 */
public record SettlementCreateCmd(Long memberId,
        java.util.List<Long> productIds,
        Long couponId) implements java.io.Serializable {}
