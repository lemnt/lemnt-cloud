package com.lemnt.mall.application.product.qry;

import java.io.Serializable;

/** 规格项分页查询。 */
public record ProductSpecPageQry(
        String name,
        int pageNum,
        int pageSize
) implements Serializable {
}
