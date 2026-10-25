package com.lemnt.mall.application.catalog.dto;

public record SpuDTO(
        Long id,
        String code,
        String name,
        Long categoryId,
        Long brandId,
        String description,
        String image,
        String status,
        java.util.List<SkuDTO> skus) {}
