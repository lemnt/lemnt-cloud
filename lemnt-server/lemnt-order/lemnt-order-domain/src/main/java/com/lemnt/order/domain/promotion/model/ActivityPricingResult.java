package com.lemnt.order.domain.promotion.model;

import com.lemnt.order.domain.settlement.model.SettlementQuote;

import java.math.BigDecimal;
import java.util.List;

/** 营销已计算的活动原始报价，订单随后附加配送并冻结。 */
public record ActivityPricingResult(
        String activityType,
        Long activityId,
        Long recordId,
        Long headId,
        Long skuId,
        Integer quantity,
        BigDecimal activityPrice,
        long usePoint,
        SettlementQuote quote,
        List<Line> lines) {
    public record Line(Long skuId, Integer quantity, BigDecimal activityPrice, long usePoint) {}
}
