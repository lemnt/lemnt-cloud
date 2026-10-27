package com.lemnt.mall.application.catalog.dto;

import java.util.List;

/**
 * 商品SPU数据传输对象。
 */
public record SpuDTO(
        Long id,
        String code,
        String name,
        Long categoryId,
        Long brandId,
        String description,
        String image,
        String status,
        List<SkuDTO> skus
) implements java.io.Serializable {}
