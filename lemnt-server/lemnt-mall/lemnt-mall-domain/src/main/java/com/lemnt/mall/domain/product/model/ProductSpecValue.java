package com.lemnt.mall.domain.product.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/** 商品规格值。 */
public record ProductSpecValue(
        Long id,
        Long specId,
        String name,
        String remark,
        LocalDateTime createTime
) implements Serializable {
}
