package com.lemnt.trade.domain.promotion.model;

public record CatalogPricingFacts(
        long skuId,
        long spuId,
        String productCode,
        String name,
        long unitPriceFen,
        int availableStock,
        String factsHash) {}
