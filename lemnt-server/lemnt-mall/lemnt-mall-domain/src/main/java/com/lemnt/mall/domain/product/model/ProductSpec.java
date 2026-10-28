package com.lemnt.mall.domain.product.model;

import java.io.Serializable;

/** 商品规格项。 */
public record ProductSpec(
        Long id,
        String name,
        String remark
) implements Serializable {
}
