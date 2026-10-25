package com.lemnt.trade.domain.checkout.model;

import java.time.Instant;
import java.util.List;

public record Settlement(
        String id,
        Long memberId,
        CheckoutQuote quote,
        List<CartReference> references,
        Instant expiresAt) {
    public Settlement {
        references = List.copyOf(references);
    }
}
