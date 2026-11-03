package com.lemnt.promotion.application.dubbo.converter;

import com.lemnt.promotion.client.dto.*;
import com.lemnt.promotion.domain.settlement.model.*;

/** 报价RPC逐字段转换，保留原优惠、规格和分摊，不使用当前配置补造字段。 */
public final class PromotionQuoteConverter {
    private PromotionQuoteConverter() {}

    public static PromotionQuoteDTO to(SettlementQuote value) {
        return value == null
                ? null
                : new PromotionQuoteDTO(
                        value.items() == null
                                ? null
                                : value.items().stream().map(PromotionQuoteConverter::to).toList(),
                        value.originalAmount(),
                        value.discountAmount(),
                        value.payableAmount(),
                        value.couponId(),
                        value.usePoint(),
                        value.givePoint(),
                        to(value.delivery()),
                        value.rewardPoint(),
                        value.freeDelivery(),
                        value.giveCouponTemplateCounts(),
                        value.promotions() == null
                                ? null
                                : value.promotions().stream()
                                        .map(PromotionQuoteConverter::to)
                                        .toList(),
                        value.deliveryOriginalPrice(),
                        value.deliveryDiscountPrice());
    }

    public static SettlementQuote from(PromotionQuoteDTO value) {
        return value == null
                ? null
                : new SettlementQuote(
                        value.items() == null
                                ? null
                                : value.items().stream()
                                        .map(PromotionQuoteConverter::from)
                                        .toList(),
                        value.originalAmount(),
                        value.discountAmount(),
                        value.payableAmount(),
                        value.couponId(),
                        value.usePoint(),
                        value.givePoint(),
                        from(value.delivery()),
                        value.rewardPoint(),
                        value.freeDelivery(),
                        value.giveCouponTemplateCounts(),
                        value.promotions() == null
                                ? null
                                : value.promotions().stream()
                                        .map(PromotionQuoteConverter::from)
                                        .toList(),
                        value.deliveryOriginalPrice(),
                        value.deliveryDiscountPrice());
    }

    public static PromotionItemDTO to(SettlementItem value) {
        return value == null
                ? null
                : new PromotionItemDTO(
                        value.productId(),
                        value.productCode(),
                        value.productName(),
                        value.unitPrice(),
                        value.quantity(),
                        value.lineAmount(),
                        value.payAmount(),
                        value.deliveryPrice(),
                        value.spuId(),
                        value.spuName(),
                        value.specs() == null
                                ? null
                                : value.specs().stream().map(PromotionQuoteConverter::to).toList(),
                        value.picUrl(),
                        value.orderItemId(),
                        value.commentStatus(),
                        value.discountAmount(),
                        value.deliveryOriginalPrice(),
                        value.deliveryDiscountPrice(),
                        value.adjustmentAmount(),
                        value.usePoint(),
                        value.givePoint(),
                        value.refundedQuantity());
    }

    public static SettlementItem from(PromotionItemDTO value) {
        return value == null
                ? null
                : new SettlementItem(
                        value.productId(),
                        value.productCode(),
                        value.productName(),
                        value.unitPrice(),
                        value.quantity(),
                        value.lineAmount(),
                        value.payAmount(),
                        value.deliveryPrice(),
                        value.spuId(),
                        value.spuName(),
                        value.specs() == null
                                ? null
                                : value.specs().stream()
                                        .map(PromotionQuoteConverter::from)
                                        .toList(),
                        value.picUrl(),
                        value.orderItemId(),
                        value.commentStatus(),
                        value.discountAmount(),
                        value.deliveryOriginalPrice(),
                        value.deliveryDiscountPrice(),
                        value.adjustmentAmount(),
                        value.usePoint(),
                        value.givePoint(),
                        value.refundedQuantity());
    }

    public static PromotionDeliveryDTO to(SettlementDelivery value) {
        return value == null
                ? null
                : new PromotionDeliveryDTO(
                        value.addressId(),
                        value.deliveryType(),
                        value.receiverName(),
                        value.receiverMobile(),
                        value.receiverAreaId(),
                        value.receiverDetailAddress(),
                        value.deliveryPrice(),
                        value.pickUpStoreId());
    }

    public static SettlementDelivery from(PromotionDeliveryDTO value) {
        return value == null
                ? null
                : new SettlementDelivery(
                        value.addressId(),
                        value.deliveryType(),
                        value.receiverName(),
                        value.receiverMobile(),
                        value.receiverAreaId(),
                        value.receiverDetailAddress(),
                        value.deliveryPrice(),
                        value.pickUpStoreId());
    }

    public static PromotionSnapshotDTO to(SettlementPromotion value) {
        return value == null
                ? null
                : new PromotionSnapshotDTO(
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
                        value.items() == null
                                ? null
                                : value.items().stream().map(PromotionQuoteConverter::to).toList());
    }

    public static SettlementPromotion from(PromotionSnapshotDTO value) {
        return value == null
                ? null
                : new SettlementPromotion(
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
                        value.items() == null
                                ? null
                                : value.items().stream()
                                        .map(PromotionQuoteConverter::from)
                                        .toList());
    }

    public static PromotionItemSnapshotDTO to(SettlementItemPromotion value) {
        return value == null
                ? null
                : new PromotionItemSnapshotDTO(
                        value.id(),
                        value.orderPromotionId(),
                        value.orderItemId(),
                        value.quoteLineKey(),
                        value.discountAmount(),
                        value.deliveryDiscountPrice(),
                        value.usePoint(),
                        value.givePoint());
    }

    public static SettlementItemPromotion from(PromotionItemSnapshotDTO value) {
        return value == null
                ? null
                : new SettlementItemPromotion(
                        value.id(),
                        value.orderPromotionId(),
                        value.orderItemId(),
                        value.quoteLineKey(),
                        value.discountAmount(),
                        value.deliveryDiscountPrice(),
                        value.usePoint(),
                        value.givePoint());
    }

    public static PromotionItemDTO.Spec to(SettlementItem.Spec v) {
        return v == null
                ? null
                : new PromotionItemDTO.Spec(
                        v.specId(), v.specName(), v.specValueId(), v.specValueName());
    }

    public static SettlementItem.Spec from(PromotionItemDTO.Spec v) {
        return v == null
                ? null
                : new SettlementItem.Spec(
                        v.specId(), v.specName(), v.specValueId(), v.specValueName());
    }
}
