package com.lemnt.order.application.settlement.service;

import com.lemnt.order.application.settlement.converter.PromotionClientConverter;
import com.lemnt.order.client.TradeOrderClient;
import com.lemnt.order.client.bo.OrderCreateBo;
import com.lemnt.order.client.dto.OrderDTO;
import com.lemnt.order.client.dto.OrderDeliveryDTO;
import com.lemnt.order.client.dto.OrderItemDTO;
import com.lemnt.order.domain.settlement.model.SettlementDelivery;
import com.lemnt.order.domain.settlement.model.SettlementItem;
import com.lemnt.order.domain.settlement.model.SettlementOrder;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 结算与订单已同域，直接注入本地订单Provider，复用既有完整报价转换，不发起Dubbo回环。
 */
@Component
@RequiredArgsConstructor
public class SettlementOrderService {
    private final TradeOrderClient client;

    public SettlementOrder save(SettlementOrder order) {
        var payload =
            new OrderCreateBo(
                order.orderNo(),
                order.memberId(),
                order.totalAmount(),
                order.items().stream()
                    .map(
                        item ->
                            new OrderItemDTO(
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
                                item.specs() == null
                                    ? null
                                    : item.specs().stream()
                                    .map(
                                        snapshotProperty ->
                                            new OrderItemDTO
                                                .Spec(
                                                snapshotProperty
                                                    .specId(),
                                                snapshotProperty
                                                    .specName(),
                                                snapshotProperty
                                                    .specValueId(),
                                                snapshotProperty
                                                    .specValueName()))
                                    .toList(),
                                item.picUrl(),
                                null,
                                null,
                                item.discountAmount(),
                                item.deliveryOriginalPrice(),
                                item.deliveryDiscountPrice(),
                                item.adjustmentAmount(),
                                item.usePoint(),
                                item.givePoint(),
                                item.refundedQuantity()))
                    .toList(),
                order.usePoint(),
                order.givePoint(),
                toDelivery(order.delivery()),
                order.giveCouponTemplateCounts(),
                order.originalAmount(),
                order.discountAmount(),
                order.deliveryOriginalPrice(),
                order.deliveryDiscountPrice(),
                order.adjustmentAmount(),
                order.pricingSnapshotStatus(),
                order.pricingSnapshotVersion(),
                order.promotions().stream().map(PromotionClientConverter::to).toList());
        return from(
            "COMPLETE".equals(order.pricingSnapshotStatus())
                ? client.createPriced(payload)
                : client.create(payload));
    }

    public SettlementOrder findByOrderNo(String orderNo) {
        return from(client.getByOrderNo(orderNo));
    }

    private SettlementOrder from(OrderDTO value) {
        return value == null
            ? null
            : new SettlementOrder(
            value.id(),
            value.orderNo(),
            value.memberId(),
            value.totalAmount(),
            value.status(),
            value.items().stream()
                .map(
                    item ->
                        new SettlementItem(
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
                            item.specs() == null
                                ? null
                                : item.specs().stream()
                                .map(
                                    snapshotProperty ->
                                        new SettlementItem
                                            .Spec(
                                            snapshotProperty
                                                .specId(),
                                            snapshotProperty
                                                .specName(),
                                            snapshotProperty
                                                .specValueId(),
                                            snapshotProperty
                                                .specValueName()))
                                .toList(),
                            item.picUrl(),
                            item.orderItemId(),
                            item.commentStatus(),
                            item.discountAmount(),
                            item.deliveryOriginalPrice(),
                            item.deliveryDiscountPrice(),
                            item.adjustmentAmount(),
                            item.usePoint(),
                            item.givePoint(),
                            item.refundedQuantity()))
                .toList(),
            value.createTime(),
            value.usePoint(),
            value.givePoint(),
            fromDelivery(value.delivery()),
            value.commentStatus(),
            value.finishTime(),
            value.giveCouponTemplateCounts(),
            value.originalAmount(),
            value.discountAmount(),
            value.deliveryOriginalPrice(),
            value.deliveryDiscountPrice(),
            value.adjustmentAmount(),
            value.pricingSnapshotStatus(),
            value.pricingSnapshotVersion(),
            value.promotions().stream().map(PromotionClientConverter::from).toList());
    }

    private OrderDeliveryDTO toDelivery(SettlementDelivery value) {
        return value == null
            ? null
            : new OrderDeliveryDTO(
            value.addressId(),
            value.deliveryType(),
            value.receiverName(),
            value.receiverMobile(),
            value.receiverAreaId(),
            value.receiverDetailAddress(),
            value.deliveryPrice(),
            value.pickUpStoreId());
    }

    private SettlementDelivery fromDelivery(OrderDeliveryDTO value) {
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
}
