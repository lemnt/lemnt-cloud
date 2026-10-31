package com.lemnt.mall.application.promotion.dto;

import com.lemnt.mall.application.promotion.cmd.PromotionOrderCmd;
import com.lemnt.mall.domain.settlement.model.SettlementQuote;

import java.math.BigDecimal;

/** 活动订单校验与计价结果。 */
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
        SettlementQuote quote
) {}
