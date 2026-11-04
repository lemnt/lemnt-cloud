package com.lemnt.product.client.dto;

import java.io.Serializable;

/**
 * 原预占来源保持真实；旧契约未提供sourceNo时保留null。
 */
public record StockReservationDTO(
    String reservationNo, Long skuId, int quantity, String status, String sourceNo)
    implements Serializable {
    public StockReservationDTO(String no, Long sku, int quantity, String status) {
        this(no, sku, quantity, status, null);
    }
}
