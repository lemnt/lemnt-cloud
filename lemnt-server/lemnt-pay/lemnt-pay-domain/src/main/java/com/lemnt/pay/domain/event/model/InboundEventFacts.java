package com.lemnt.pay.domain.event.model;

import java.util.List;

/** 支付消费的关单事实，库存行仅保留原协议数据。 */
public final class InboundEventFacts {
    private InboundEventFacts() {}

    public record Closed(String orderNo, Long memberId, List<StockLine> items) {}

    public record StockLine(Long skuId, int quantity) {}
}
