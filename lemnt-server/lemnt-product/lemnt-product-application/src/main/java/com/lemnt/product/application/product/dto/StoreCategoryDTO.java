package com.lemnt.product.application.product.dto;

import java.io.Serializable;

/**
 * 门店分类数据传输对象。
 */
public record StoreCategoryDTO(Long id, Long parentId, String name, String picUrl)
    implements Serializable {
}
