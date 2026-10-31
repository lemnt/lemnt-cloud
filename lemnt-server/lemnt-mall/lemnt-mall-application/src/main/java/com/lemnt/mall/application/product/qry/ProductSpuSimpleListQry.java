package com.lemnt.mall.application.product.qry;

import java.io.Serializable;

/** 在售商品选择查询。 */
public record ProductSpuSimpleListQry(
        String name,
        int limit
) implements Serializable {
}
