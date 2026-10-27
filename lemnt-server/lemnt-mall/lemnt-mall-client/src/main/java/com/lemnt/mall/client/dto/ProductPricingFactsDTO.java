package com.lemnt.mall.client.dto;

import java.io.Serializable;

/**
 * 商品计价事实数据传输对象。
 */
public record ProductPricingFactsDTO(
        String factsProtocol,
        Long skuId,
        Long spuId,
        String productCode,
        String name,
        long unitPriceFen,
        int availableStock,
        String factsHash
) implements Serializable {}
