package com.lemnt.order.application.settlement.dto;

import java.io.Serializable;
import java.time.Instant;

/**
 * 结算数据传输对象。
 */
public record SettlementDTO(String settlementId, Instant expiresAt, SettlementQuoteDTO quote)
        implements Serializable {}
