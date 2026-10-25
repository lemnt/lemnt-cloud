package com.lemnt.trade.domain.store.model;

public record CartAddition(
        Long memberId,
        String requestNo,
        Long productId,
        int quantity,
        com.lemnt.trade.domain.checkout.model.CartLine result) {}
