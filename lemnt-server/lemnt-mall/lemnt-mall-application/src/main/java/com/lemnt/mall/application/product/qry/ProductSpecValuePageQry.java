package com.lemnt.mall.application.product.qry;

import java.io.Serializable;

/** 规格值分页查询。 */
public record ProductSpecValuePageQry(
        Long specId,
        String name,
        int pageNum,
        int pageSize
) implements Serializable {
}
