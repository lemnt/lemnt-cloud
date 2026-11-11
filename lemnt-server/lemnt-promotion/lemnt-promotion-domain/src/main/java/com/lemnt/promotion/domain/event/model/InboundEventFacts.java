package com.lemnt.promotion.domain.event.model;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/** 营销消费的外部事实视图，不携带外部Client类型。 */
public final class InboundEventFacts {
    private InboundEventFacts() {}

    public record Paid(
        String orderNo,
        Long memberId,
        BigDecimal amount,
        long usePoint,
        long givePoint,
        Map<Long, Integer> giveCouponTemplateCounts,
        List<StockLine> items) {}

    public record Closed(String orderNo, Long memberId, List<StockLine> items) {}

    public record Completed(String orderNo, Long memberId) {}

    public record AfterSaleCompleted(
        String orderNo,
        String afterSaleNo,
        Long memberId,
        Long orderItemId,
        Long skuId,
        int quantity,
        BigDecimal amount,
        long returnUsePoint,
        long deductGivePoint,
        int ruleVersion,
        boolean fullyRefunded) {}

    public record StockPreReserved(String reservationNo, Long skuId, int quantity) {}

    public record StockLine(Long skuId, int quantity) {}
}
