package com.lemnt.mall.application.product.dto;

import java.io.Serializable;
import java.util.List;

/**
 * 门店详情数据传输对象。
 */
public record StoreDetailDTO(
        Long id,
        String name,
        Long categoryId,
        Long brandId,
        String description,
        String image,
        List<StoreSkuDTO> skus)
        implements Serializable {}
