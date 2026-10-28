package com.lemnt.mall.application.product.qry;

import java.io.Serializable;

/** 品牌分页查询。 */
public record BrandPageQry(
        String name,
        String status,
        int pageNum,
        int pageSize
) implements Serializable {
}
