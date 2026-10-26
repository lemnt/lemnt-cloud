package com.lemnt.mall.client.dto;

public record ProductPricingFactsDTO(
        String factsProtocol,
        Long skuId,
        Long spuId,
        String productCode,
        String name,
        long unitPriceFen,
        int availableStock,
        String factsHash)
        implements java.io.Serializable {}
