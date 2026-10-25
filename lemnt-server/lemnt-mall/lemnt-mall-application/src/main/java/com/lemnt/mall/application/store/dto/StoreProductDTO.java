package com.lemnt.mall.application.store.dto;

public record StoreProductDTO(
        Long id,
        String name,
        Long categoryId,
        Long brandId,
        String image,
        String minPrice,
        String maxPrice,
        boolean available) {}
