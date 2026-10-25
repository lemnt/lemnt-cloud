package com.lemnt.mall.application.store.dto;

public record StoreDetailDTO(
        Long id,
        String name,
        Long categoryId,
        Long brandId,
        String description,
        String image,
        java.util.List<StoreSkuDTO> skus) {}
