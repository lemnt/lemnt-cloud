package com.lemnt.mall.application.settlement.dto;

/**
 * 结算放弃数据传输对象。
 */
public record SettlementAbandonDTO(
        boolean abandoned,
        SettlementReceiptDTO order
) implements java.io.Serializable {}
