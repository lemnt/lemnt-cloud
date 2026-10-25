package com.lemnt.mall.application.catalog.dto;

public record CategoryDTO(
        Long id, Long parentId, String name, String picUrl, Integer sort, String status) {}
