package com.lemnt.mall.application.product.dto;

import java.util.Map;

/**
 * 门店商品SKU数据传输对象。
 */
public record StoreSkuDTO(
        Long id,
        String name,
        String price,
        int stock,
        String image,
        Map<String,
        String> properties,
        Map<Long, Long> specValueIds
) implements java.io.Serializable {}
