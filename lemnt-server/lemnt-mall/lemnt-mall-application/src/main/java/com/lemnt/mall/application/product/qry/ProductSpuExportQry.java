package com.lemnt.mall.application.product.qry;

import java.io.Serializable;

/** 商品导出独立查询条件，不接受分页参数。 */
public record ProductSpuExportQry(String name, String status, Long categoryId, Long brandId,
        String stockStatus) implements Serializable {
}
