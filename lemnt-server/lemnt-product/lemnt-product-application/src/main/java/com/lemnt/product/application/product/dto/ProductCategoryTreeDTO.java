package com.lemnt.product.application.product.dto;

import java.io.Serializable;
import java.util.List;

/**
 * 后台分类树。
 */
public record ProductCategoryTreeDTO(
    Long id,
    String name,
    String picUrl,
    Integer sort,
    String status,
    List<ProductCategoryTreeDTO> children)
    implements Serializable {
}
