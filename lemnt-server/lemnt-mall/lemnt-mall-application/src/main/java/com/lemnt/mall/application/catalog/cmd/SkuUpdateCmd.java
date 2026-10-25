package com.lemnt.mall.application.catalog.cmd;

public record SkuUpdateCmd(Long id, String name, java.math.BigDecimal price, String image) {}
