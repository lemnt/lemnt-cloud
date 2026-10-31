package com.lemnt.mall.application.product.qry;

/**
 * 商品SPU分页查询。
 */
public record ProductSpuPageQry(
        String name,
        String status,
        int pageNum,
        int pageSize,
        Long categoryId,
        Long brandId,
        String stockStatus
) implements java.io.Serializable {}
