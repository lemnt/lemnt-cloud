package com.lemnt.product.client.dto;

import java.io.Serializable;

/** 未查到返回null；不能把未知预占状态解释为已确认。 */
public record StockReservationDTO(String reservationNo, Long skuId, int quantity, String status)
        implements Serializable {}
