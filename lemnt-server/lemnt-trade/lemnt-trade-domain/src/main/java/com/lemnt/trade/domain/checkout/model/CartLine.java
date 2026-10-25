package com.lemnt.trade.domain.checkout.model;

public record CartLine(
        Long id, Long memberId, Long productId, int quantity, boolean selected, long version) {}
