package com.lemnt.product.application.product.dto;

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
        List<StoreSkuDTO> skus,
        Integer salesCount,
        String introduction,
        List<String> sliderPicUrls,
        List<Integer> deliveryTypes,
        Long deliveryTemplateId,
        Long displaySalesCount)
        implements Serializable {
    /** 兼容旧调用，新增字段保持未知。 */
    public StoreDetailDTO(
            Long id,
            String name,
            Long categoryId,
            Long brandId,
            String description,
            String image,
            List<StoreSkuDTO> skus,
            Integer salesCount,
            String introduction,
            List<String> sliderPicUrls,
            List<Integer> deliveryTypes,
            Long deliveryTemplateId) {
        this(
                id,
                name,
                categoryId,
                brandId,
                description,
                image,
                skus,
                salesCount,
                introduction,
                sliderPicUrls,
                deliveryTypes,
                deliveryTemplateId,
                null);
    }

    /** 兼容未携带展示及配送字段的内部调用；未知字段保持null。 */
    public StoreDetailDTO(
            Long id,
            String name,
            Long categoryId,
            Long brandId,
            String description,
            String image,
            List<StoreSkuDTO> skus,
            Integer salesCount) {
        this(
                id,
                name,
                categoryId,
                brandId,
                description,
                image,
                skus,
                salesCount,
                null,
                null,
                null,
                null);
    }
}
