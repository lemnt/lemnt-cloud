package com.lemnt.mall.domain.product.model;

import java.io.Serializable;

/** 库存预占记录，与库存主表分开维护。 */
public record StockReservation(String reservationNo,
        Long productId,
        Integer quantity,
        String status, String sourceNo) implements Serializable {
    public StockReservation(String reservationNo, Long productId, Integer quantity, String status) {
        this(reservationNo, productId, quantity, status, null);
    }
}
