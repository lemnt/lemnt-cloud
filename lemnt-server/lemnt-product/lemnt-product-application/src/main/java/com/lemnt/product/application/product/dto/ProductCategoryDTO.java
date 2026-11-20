package com.lemnt.product.application.product.dto;

import java.io.Serializable;

/**
 * 分类数据传输对象。
 */
public record ProductCategoryDTO(
    Long id, Long parentId, String name, String picUrl, Integer sort, String status)
    implements Serializable {
}
