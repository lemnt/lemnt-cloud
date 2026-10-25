package com.lemnt.trade.application.checkout.dto;

public record SettlementDTO(
        String settlementId, java.time.Instant expiresAt, CheckoutQuoteDTO quote) {}
