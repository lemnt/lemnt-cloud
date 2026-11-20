package com.lemnt.product.domain.product.model;

import java.io.Serializable;

/**
 * 库存预占记录，与库存主表分开维护。
 */
public record ProductStockReservation(
        String reservationNo,
        Long productId,
        Integer quantity,
        String status,
        String sourceNo,
        Boolean salesRecorded)
        implements Serializable {
    public static final String CANCELLED_BEFORE_RESERVE = "CANCELLED";
    public static final String RELEASED_AFTER_RESERVE = "RELEASED_RESERVED";

    /** 已释放的旧记录保留原状态，释放调用仍可幂等重放。 */
    public boolean terminalRelease() {
        return "RELEASED".equals(status)
                || RELEASED_AFTER_RESERVE.equals(status)
                || CANCELLED_BEFORE_RESERVE.equals(status);
    }

    public ProductStockReservation(
            String reservationNo, Long productId, Integer quantity, String status) {
        this(reservationNo, productId, quantity, status, null, null);
    }

    public ProductStockReservation(
            String reservationNo,
            Long productId,
            Integer quantity,
            String status,
            String sourceNo) {
        this(reservationNo, productId, quantity, status, sourceNo, null);
    }
}
