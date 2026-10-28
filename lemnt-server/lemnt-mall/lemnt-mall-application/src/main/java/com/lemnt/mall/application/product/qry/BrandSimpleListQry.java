package com.lemnt.mall.application.product.qry;

import java.io.Serializable;

/** 启用品牌选择查询。 */
public record BrandSimpleListQry(
        String name,
        int limit
) implements Serializable {
}
