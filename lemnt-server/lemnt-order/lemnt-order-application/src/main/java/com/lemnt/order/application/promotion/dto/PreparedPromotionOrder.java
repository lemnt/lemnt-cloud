package com.lemnt.order.application.promotion.dto;

import com.lemnt.order.domain.promotion.model.PromotionOrder;
import com.lemnt.order.domain.settlement.model.SettlementOrder;

/** 活动订单准备结果。 */
public record PreparedPromotionOrder(
        SettlementOrder order, PromotionOrder promotionOrder, boolean created) {}
