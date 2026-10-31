package com.lemnt.mall.application.promotion.dto;

import com.lemnt.mall.application.promotion.cmd.PromotionOrderCmd;
import com.lemnt.mall.domain.settlement.model.SettlementQuote;

import java.math.BigDecimal;
import java.util.List;

/** 活动订单校验与计价结果，lines 保存每行活动价与积分。 */
public record PromotionOrderDraft(
        PromotionOrderCmd cmd,
        String activityType,
        Long activityId,
        Long recordId,
        Long headId,
        Long skuId,
        Integer quantity,
        BigDecimal activityPrice,
        long usePoint,
        SettlementQuote quote,
        List<Line> lines
) {
    /** 活动订单商品行计价快照。 */
    public record Line(Long skuId, Integer quantity, BigDecimal activityPrice, long usePoint) {}
}
