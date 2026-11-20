package com.lemnt.pay.client.dto;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 钱包快照，所有金额单位元。
 */
public record WalletDTO(
    Long memberId,
    BigDecimal balance,
    BigDecimal freezePrice,
    BigDecimal totalExpense,
    BigDecimal totalRecharge)
    implements Serializable {
}
