package com.lemnt.mall.application.catalog.dto;

public record BrandDTO(
        Long id, String name, String picUrl, String description, Integer sort, String status) {}
