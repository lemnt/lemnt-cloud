package com.lemnt.mall.application.product.dto;

import java.io.Serializable;

/**
 * 品牌数据传输对象。
 */
public record BrandDTO(
        Long id, String name, String picUrl, String description, Integer sort, String status)
        implements Serializable {}
