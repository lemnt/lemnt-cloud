package com.lemnt.mall.application.store.qry;

/**
 * 门店分页查询。
 */
public record StorePageQry(
        String name,
        Long categoryId,
        int pageNum,
        int pageSize
) implements java.io.Serializable {}
