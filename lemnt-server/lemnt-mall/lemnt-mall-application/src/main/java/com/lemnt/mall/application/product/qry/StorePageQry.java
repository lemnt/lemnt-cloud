package com.lemnt.mall.application.product.qry;

import java.io.Serializable;

/**
 * 门店分页查询。
 */
public record StorePageQry(String name, Long categoryId, int pageNum, int pageSize)
        implements Serializable {}
