package com.lemnt.mall.application.product.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

/** 规格项应用结果。 */
public record ProductSpecDTO(
        Long id,
        String name,
        String remark,
        LocalDateTime createTime
) implements Serializable {
}
