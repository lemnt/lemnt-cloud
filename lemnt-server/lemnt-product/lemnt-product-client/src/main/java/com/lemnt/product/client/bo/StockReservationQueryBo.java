package com.lemnt.product.client.bo;

import java.io.Serializable;

/** 使用原预占号和SKU查询库存确认结果。 */
public record StockReservationQueryBo(String reservationNo, Long skuId) implements Serializable {}
