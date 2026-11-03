package com.lemnt.trade.infrastructure.promotion.converter;

import com.lemnt.trade.domain.promotion.model.OrderItemPromotion;
import com.lemnt.trade.domain.promotion.model.OrderPromotion;
import com.lemnt.trade.domain.promotion.model.OrderPromotionRefund;
import com.lemnt.trade.infrastructure.promotion.dataobject.OrderItemPromotionDO;
import com.lemnt.trade.infrastructure.promotion.dataobject.OrderPromotionDO;
import com.lemnt.trade.infrastructure.promotion.dataobject.OrderPromotionRefundDO;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;

import java.util.List;
import java.util.Map;

/** 优惠数据对象与不可变领域快照转换。 */
public final class OrderPromotionConverter {
    private OrderPromotionConverter() {}

    /** 写入OrderPromotion的冻结字段，不回查业务规则。 */
    public static OrderPromotionDO toDO(OrderPromotion value) {
        return new OrderPromotionDO()
                .setId(value.id())
                .setOrderId(value.orderId())
                .setPromotionKey(value.promotionKey())
                .setPromotionType(value.promotionType())
                .setSourceId(value.sourceId())
                .setPromotionName(value.promotionName())
                .setRuleSummary(value.ruleSummary())
                .setRuleSnapshot(value.ruleSnapshot())
                .setDiscountAmount(value.discountAmount())
                .setDeliveryDiscountPrice(value.deliveryDiscountPrice())
                .setUsePoint(value.usePoint())
                .setGivePoint(value.givePoint())
                .setGiveCouponTemplateCounts(
                        value.giveCouponTemplateCounts() == null
                                ? null
                                : JSON.toJSONString(value.giveCouponTemplateCounts()));
    }

    /** 读取OrderPromotion的原始结果，保留历史未知。 */
    public static OrderPromotion from(OrderPromotionDO row, List<OrderItemPromotion> items) {
        return new OrderPromotion(
                row.getId(),
                row.getOrderId(),
                row.getPromotionKey(),
                row.getPromotionType(),
                row.getSourceId(),
                row.getPromotionName(),
                row.getRuleSummary(),
                row.getRuleSnapshot(),
                row.getDiscountAmount(),
                row.getDeliveryDiscountPrice(),
                row.getUsePoint(),
                row.getGivePoint(),
                row.getGiveCouponTemplateCounts() == null
                        ? null
                        : JSON.parseObject(
                                row.getGiveCouponTemplateCounts(),
                                new TypeReference<Map<Long, Integer>>() {}),
                items);
    }

    /** 写入OrderItemPromotion的冻结字段，不回查业务规则。 */
    public static OrderItemPromotionDO toDO(OrderItemPromotion value) {
        return new OrderItemPromotionDO()
                .setId(value.id())
                .setOrderPromotionId(value.orderPromotionId())
                .setOrderItemId(value.orderItemId())
                .setDiscountAmount(value.discountAmount())
                .setDeliveryDiscountPrice(value.deliveryDiscountPrice())
                .setUsePoint(value.usePoint())
                .setGivePoint(value.givePoint());
    }

    /** 读取OrderItemPromotion的原始结果，保留历史未知。 */
    public static OrderItemPromotion from(OrderItemPromotionDO row, String quoteLineKey) {
        return new OrderItemPromotion(
                row.getId(),
                row.getOrderPromotionId(),
                row.getOrderItemId(),
                quoteLineKey,
                row.getDiscountAmount(),
                row.getDeliveryDiscountPrice(),
                row.getUsePoint(),
                row.getGivePoint());
    }

    /** 写入OrderPromotionRefund的冻结字段，不回查业务规则。 */
    public static OrderPromotionRefundDO toDO(OrderPromotionRefund value) {
        return new OrderPromotionRefundDO()
                .setId(value.id())
                .setAfterSaleId(value.afterSaleId())
                .setOrderItemPromotionId(value.orderItemPromotionId())
                .setRefundDiscountAmount(value.refundDiscountAmount())
                .setRefundDeliveryDiscountPrice(value.refundDeliveryDiscountPrice())
                .setReturnUsePoint(value.returnUsePoint())
                .setDeductGivePoint(value.deductGivePoint());
    }

    /** 读取OrderPromotionRefund的原始结果，保留历史未知。 */
    public static OrderPromotionRefund from(OrderPromotionRefundDO row) {
        return new OrderPromotionRefund(
                row.getId(),
                row.getAfterSaleId(),
                row.getOrderItemPromotionId(),
                row.getRefundDiscountAmount(),
                row.getRefundDeliveryDiscountPrice(),
                row.getReturnUsePoint(),
                row.getDeductGivePoint());
    }
}
