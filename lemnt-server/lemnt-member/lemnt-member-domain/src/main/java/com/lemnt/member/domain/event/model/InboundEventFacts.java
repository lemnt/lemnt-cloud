package com.lemnt.member.domain.event.model;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/** 会员消费的入站订单事实，不暴露外部服务协议类型。 */
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

    public record Shipped(String orderNo, Long memberId) {}

    public record StockLine(Long skuId, int quantity) {}
}
