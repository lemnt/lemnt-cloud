package com.lemnt.mall.domain.product.model;

/** 商品库存查询分组，与商品上下架状态分开；警戒值沿用芋道。 */
public enum ProductStockStatus {
    SOLD_OUT,
    LOW_STOCK;

    public static final int ALERT_STOCK = 10;
}
