package com.lemnt.product.application.product.qry;

import java.io.Serializable;
import java.util.List;

/**
 * 门店分页查询。
 */
public record StorePageQry(
        String name,
        Long categoryId,
        int pageNum,
        int pageSize,
        List<Long> categoryIds,
        String sortField,
        Boolean sortAsc)
        implements Serializable {
    /** 兼容原单分类分页调用。 */
    public StorePageQry(String name, Long categoryId, int pageNum, int pageSize) {
        this(name, categoryId, pageNum, pageSize, null, null, null);
    }
}
