package com.lemnt.mall.application.product.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

/** 规格值应用结果。 */
public record ProductSpecValueDTO(
        Long id,
        Long specId,
        String name,
        String remark,
        LocalDateTime createTime
) implements Serializable {
}
