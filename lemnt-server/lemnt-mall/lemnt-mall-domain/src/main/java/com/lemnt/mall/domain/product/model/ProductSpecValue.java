package com.lemnt.mall.domain.product.model;

import java.io.Serializable;

/** 商品规格值。 */
public record ProductSpecValue(
        Long id,
        Long specId,
        String name,
        String remark
) implements Serializable {
}
