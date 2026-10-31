package com.lemnt.mall.domain.settlement.model;

/** 提交后的结算单与重试上下文，一并写入缓存。 */
public record SettlementSubmission(SettlementOrder order, SettlementRecord record) {
}
