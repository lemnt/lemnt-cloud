package com.lemnt.order.domain.promotion.gateway;

import com.lemnt.order.domain.promotion.model.ActivityPricingRequest;
import com.lemnt.order.domain.promotion.model.ActivityPricingResult;
import com.lemnt.order.domain.settlement.model.SettlementItem;
import com.lemnt.order.domain.settlement.model.SettlementQuote;

import java.util.List;

/**
 * 仅在新报价阶段调用；已经提交的订单和退款使用本地冻结结果。
 */
public interface PromotionPricingGateway {
    SettlementQuote price(
        Long memberId, List<SettlementItem> items, Long couponId, boolean pointStatus);

    ActivityPricingResult priceActivity(ActivityPricingRequest request);
}
