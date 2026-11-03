package com.lemnt.order.application.order.service;

import com.lemnt.order.domain.order.model.TradeOrder;
import com.lemnt.order.domain.order.model.TradeOrderDelivery;
import com.lemnt.order.domain.order.model.TradeOrderItem;
import com.lemnt.order.domain.promotion.model.OrderItemPromotion;
import com.lemnt.order.domain.promotion.model.OrderPromotion;
import com.lemnt.order.domain.promotion.service.OrderPricingRules;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Objects;

/** 订单与明细的本地持久化事务，库存由调用方在提交后同步预占。 */
@Component
@RequiredArgsConstructor
public class OrderSubmissionService {
    private final OrderPersistenceService orders;

    /** 重试使用同一个订单号，订单内容不一致时拒绝。 */
    @Transactional(rollbackFor = Exception.class)
    public TradeOrder create(TradeOrder order) {
        OrderPricingRules.validate(order);
        var previous = orders.findByOrderNoForUpdate(order.orderNo());
        if (previous != null) {
            previous = orders.findByOrderNo(order.orderNo());
            requireSame(previous, order);
            return previous;
        }
        var saved = orders.save(order);
        return saved;
    }

    /** 验证幂等重试没有替换会员、金额或商品快照。 */
    public void requireSame(TradeOrder previous, TradeOrder order) {
        if (!previous.memberId().equals(order.memberId())
                || previous.totalAmount().compareTo(order.totalAmount()) != 0
                || previous.usePoint() != order.usePoint()
                || previous.givePoint() != order.givePoint()
                || !Objects.equals(
                        previous.giveCouponTemplateCounts(), order.giveCouponTemplateCounts())
                || !Objects.equals(
                        purchaseDelivery(previous.delivery()), purchaseDelivery(order.delivery()))
                || !Objects.equals(previous.originalAmount(), order.originalAmount())
                || !Objects.equals(previous.discountAmount(), order.discountAmount())
                || !Objects.equals(previous.deliveryOriginalPrice(), order.deliveryOriginalPrice())
                || !Objects.equals(previous.deliveryDiscountPrice(), order.deliveryDiscountPrice())
                || !Objects.equals(previous.adjustmentAmount(), order.adjustmentAmount())
                || !Objects.equals(previous.pricingSnapshotStatus(), order.pricingSnapshotStatus())
                || !promotionValues(previous.promotions())
                        .equals(promotionValues(order.promotions()))
                || !purchaseItems(previous.items()).equals(purchaseItems(order.items()))) {
            throw new IllegalStateException("订单号已用于不同的订单内容");
        }
    }

    /** 核销码由服务端生成，重试重算不应被视为内容变化。 */
    private TradeOrderDelivery purchaseDelivery(TradeOrderDelivery value) {
        return value == null
                ? null
                : new TradeOrderDelivery(
                        value.addressId(),
                        value.deliveryType(),
                        value.receiverName(),
                        value.receiverMobile(),
                        value.receiverAreaId(),
                        value.receiverDetailAddress(),
                        value.deliveryPrice(),
                        value.pickUpStoreId(),
                        null);
    }

    /** 数据库主键及评价状态不属于原购买快照，不能影响下单原请求重试。 */
    private List<TradeOrderItem> purchaseItems(List<TradeOrderItem> values) {
        return values.stream()
                .map(
                        item ->
                                new TradeOrderItem(
                                        item.productId(),
                                        item.productCode(),
                                        item.productName(),
                                        item.unitPrice(),
                                        item.quantity(),
                                        item.lineAmount(),
                                        item.payAmount(),
                                        item.deliveryPrice(),
                                        item.spuId(),
                                        item.spuName(),
                                        item.specs(),
                                        item.picUrl(),
                                        null,
                                        null,
                                        item.discountAmount(),
                                        item.deliveryOriginalPrice(),
                                        item.deliveryDiscountPrice(),
                                        item.adjustmentAmount(),
                                        item.usePoint(),
                                        item.givePoint(),
                                        null))
                .toList();
    }

    /** 原请求重试忽略数据库生成的优惠、行主键，仍核对全部计算结果和来源。 */
    private List<OrderPromotion> promotionValues(List<OrderPromotion> values) {
        return values.stream()
                .map(
                        p ->
                                new OrderPromotion(
                                        null,
                                        null,
                                        p.promotionKey(),
                                        p.promotionType(),
                                        p.sourceId(),
                                        p.promotionName(),
                                        p.ruleSummary(),
                                        p.ruleSnapshot(),
                                        p.discountAmount(),
                                        p.deliveryDiscountPrice(),
                                        p.usePoint(),
                                        p.givePoint(),
                                        p.giveCouponTemplateCounts(),
                                        p.items().stream()
                                                .map(
                                                        v ->
                                                                new OrderItemPromotion(
                                                                        null,
                                                                        null,
                                                                        null,
                                                                        v.quoteLineKey(),
                                                                        v.discountAmount(),
                                                                        v.deliveryDiscountPrice(),
                                                                        v.usePoint(),
                                                                        v.givePoint()))
                                                .toList()))
                .toList();
    }
}
