package com.lemnt.mall.application.product.dto;

import java.io.Serializable;
import java.util.List;

/** 后台分类树。 */
public record CategoryTreeDTO(
        Long id,
        String name,
        String picUrl,
        Integer sort,
        String status,
        List<CategoryTreeDTO> children
) implements Serializable {
}
