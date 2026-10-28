package com.lemnt.mall.application.product.dto;

/**
 * 门店商品数据传输对象。
 */
public record StoreProductDTO(
        Long id,
        String name,
        Long categoryId,
        Long brandId,
        String image,
        String minPrice,
        String maxPrice,
        boolean available
) implements java.io.Serializable {}
