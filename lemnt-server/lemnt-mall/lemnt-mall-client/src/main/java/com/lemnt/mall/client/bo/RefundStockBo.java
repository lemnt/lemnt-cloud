package com.lemnt.mall.client.bo;

/** 售后成功后归还原订单 SKU 数量。 */
public record RefundStockBo(Long productId, Integer quantity) implements java.io.Serializable {}
