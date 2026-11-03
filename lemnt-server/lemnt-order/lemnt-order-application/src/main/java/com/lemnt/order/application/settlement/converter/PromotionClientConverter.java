package com.lemnt.order.application.settlement.converter;

import com.lemnt.order.domain.settlement.model.SettlementItemPromotion;
import com.lemnt.order.domain.settlement.model.SettlementPromotion;
import com.lemnt.trade.client.dto.OrderItemPromotionDTO;
import com.lemnt.trade.client.dto.OrderPromotionDTO;

/** 优惠RPC与本模块领域类型的完整边界转换。 */
public final class PromotionClientConverter {
    private PromotionClientConverter() {}

    /** 接收冻结结果，不重新定价。 */
    public static SettlementPromotion from(OrderPromotionDTO value) {
        return new SettlementPromotion(
                value.id(),
                value.orderId(),
                value.promotionKey(),
                value.promotionType(),
                value.sourceId(),
                value.promotionName(),
                value.ruleSummary(),
                value.ruleSnapshot(),
                value.discountAmount(),
                value.deliveryDiscountPrice(),
                value.usePoint(),
                value.givePoint(),
                value.giveCouponTemplateCounts(),
                value.items().stream()
                        .map(
                                item ->
                                        new SettlementItemPromotion(
                                                item.id(),
                                                item.orderPromotionId(),
                                                item.orderItemId(),
                                                item.quoteLineKey(),
                                                item.discountAmount(),
                                                item.deliveryDiscountPrice(),
                                                item.usePoint(),
                                                item.givePoint()))
                        .toList());
    }

    /** 发送冻结结果，不重新定价。 */
    public static OrderPromotionDTO to(SettlementPromotion value) {
        return new OrderPromotionDTO(
                value.id(),
                value.orderId(),
                value.promotionKey(),
                value.promotionType(),
                value.sourceId(),
                value.promotionName(),
                value.ruleSummary(),
                value.ruleSnapshot(),
                value.discountAmount(),
                value.deliveryDiscountPrice(),
                value.usePoint(),
                value.givePoint(),
                value.giveCouponTemplateCounts(),
                value.items().stream()
                        .map(
                                item ->
                                        new OrderItemPromotionDTO(
                                                item.id(),
                                                item.orderPromotionId(),
                                                item.orderItemId(),
                                                item.quoteLineKey(),
                                                item.discountAmount(),
                                                item.deliveryDiscountPrice(),
                                                item.usePoint(),
                                                item.givePoint()))
                        .toList());
    }
}
