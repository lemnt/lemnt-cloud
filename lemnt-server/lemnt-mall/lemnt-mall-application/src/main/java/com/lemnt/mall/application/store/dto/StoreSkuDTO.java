package com.lemnt.mall.application.store.dto;

public record StoreSkuDTO(
        Long id,
        String name,
        String price,
        int stock,
        String image,
        java.util.Map<String, String> properties) {}
