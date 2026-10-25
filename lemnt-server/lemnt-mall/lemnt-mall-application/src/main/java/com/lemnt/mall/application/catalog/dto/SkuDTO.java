package com.lemnt.mall.application.catalog.dto;

public record SkuDTO(
        Long id,
        Long spuId,
        String code,
        String name,
        java.math.BigDecimal price,
        Integer stock,
        String image,
        java.util.Map<String, String> properties,
        String status) {}
