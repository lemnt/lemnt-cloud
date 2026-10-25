package com.lemnt.mall.domain.catalog.model;

public record Brand(
        Long id, String name, String picUrl, String description, Integer sort, String status) {}
