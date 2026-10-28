package com.lemnt.mall.application.product.qry;

import java.io.Serializable;

/** 规格字典选择查询。 */
public record ProductSpecValueSimpleListQry(
        Long specId,
        String name,
        int limit
) implements Serializable {
}
