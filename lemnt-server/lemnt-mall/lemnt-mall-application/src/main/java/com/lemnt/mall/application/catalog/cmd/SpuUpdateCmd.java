package com.lemnt.mall.application.catalog.cmd;

public record SpuUpdateCmd(
        Long id, String name, Long categoryId, Long brandId, String description, String image) {}
