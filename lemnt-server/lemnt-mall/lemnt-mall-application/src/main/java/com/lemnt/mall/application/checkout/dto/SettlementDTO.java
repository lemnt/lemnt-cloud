package com.lemnt.mall.application.checkout.dto;

import java.time.Instant;

/**
 * 结算数据传输对象。
 */
public record SettlementDTO(
        String settlementId,
        Instant expiresAt,
        CheckoutQuoteDTO quote
) implements java.io.Serializable {}
