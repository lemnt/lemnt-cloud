package com.lemnt.mall.application.product.dto;

import java.io.Serializable;

/** 规格值应用结果。 */
public record ProductSpecValueDTO(
        Long id,
        Long specId,
        String name,
        String remark
) implements Serializable {
}
