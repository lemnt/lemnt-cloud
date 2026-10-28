package com.lemnt.mall.application.product.dto;

import java.io.Serializable;

/** 规格项应用结果。 */
public record ProductSpecDTO(
        Long id,
        String name,
        String remark
) implements Serializable {
}
