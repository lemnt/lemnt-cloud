package com.lemnt.mall.application.catalog.qry;

/**
 * 商品SPU分页查询。
 */
public record SpuPageQry(
        String name,
        String status,
        int pageNum,
        int pageSize
) implements java.io.Serializable {}
