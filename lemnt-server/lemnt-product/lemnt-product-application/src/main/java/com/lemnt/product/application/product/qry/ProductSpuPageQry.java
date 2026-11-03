package com.lemnt.product.application.product.qry;

import java.io.Serializable;

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
        String stockStatus)
        implements Serializable {}
