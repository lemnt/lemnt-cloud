package com.lemnt.pay.client.dto;

/** 钱包快照，所有金额单位元。 */
public record WalletDTO(
        Long memberId,
        java.math.BigDecimal balance,
        java.math.BigDecimal freezePrice,
        java.math.BigDecimal totalExpense,
        java.math.BigDecimal totalRecharge)
        implements java.io.Serializable {}
