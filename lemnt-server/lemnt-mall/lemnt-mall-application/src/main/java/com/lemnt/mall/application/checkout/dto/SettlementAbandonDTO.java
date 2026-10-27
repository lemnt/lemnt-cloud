package com.lemnt.mall.application.checkout.dto;

/**
 * 结算放弃数据传输对象。
 */
public record SettlementAbandonDTO(
        boolean abandoned,
        CheckoutReceiptDTO order
) implements java.io.Serializable {}
