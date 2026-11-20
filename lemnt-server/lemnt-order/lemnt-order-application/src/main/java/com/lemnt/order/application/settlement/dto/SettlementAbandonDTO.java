package com.lemnt.order.application.settlement.dto;

import java.io.Serializable;

/**
 * 结算放弃数据传输对象。
 */
public record SettlementAbandonDTO(boolean abandoned, SettlementReceiptDTO order)
    implements Serializable {
}
