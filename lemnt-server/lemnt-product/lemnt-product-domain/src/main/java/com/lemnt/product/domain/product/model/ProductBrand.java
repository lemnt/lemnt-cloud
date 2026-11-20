package com.lemnt.product.domain.product.model;

/**
 * 品牌。
 */
public record ProductBrand(
    Long id, String name, String picUrl, String description, Integer sort, String status) {
}
