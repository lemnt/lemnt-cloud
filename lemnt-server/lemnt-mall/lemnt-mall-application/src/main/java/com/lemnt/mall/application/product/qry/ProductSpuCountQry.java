package com.lemnt.mall.application.product.qry;

import java.io.Serializable;

/** 商品状态统计条件。 */
public record ProductSpuCountQry(
        String name,
        Long categoryId,
        Long brandId
) implements Serializable {
}
