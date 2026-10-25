package com.lemnt.trade.application.store.dto;

public record CartAdditionDTO(
        String requestNo, Long productId, int addedQuantity, int resultingQuantity) {}
