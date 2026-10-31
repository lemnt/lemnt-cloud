package com.lemnt.mall.domain.product.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/** 商品规格项。 */
public record ProductSpec(
        Long id,
        String name,
        String remark,
        LocalDateTime createTime
) implements Serializable {
}
