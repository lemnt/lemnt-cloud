package com.lemnt.mall.application.promotion.dto;

import com.lemnt.mall.domain.promotion.model.PromotionOrder;
import com.lemnt.mall.domain.settlement.model.SettlementOrder;

/** 活动订单准备结果。 */
public record PreparedPromotionOrder(
        SettlementOrder order,
        PromotionOrder promotionOrder,
        boolean created
) {}
