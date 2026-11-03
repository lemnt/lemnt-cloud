package com.lemnt.mall.infrastructure.settlement.gateway;

import com.lemnt.mall.domain.product.model.CommentOrderRequest;
import com.lemnt.mall.domain.settlement.gateway.OrderGateway;
import com.lemnt.mall.domain.settlement.model.SettlementDelivery;
import com.lemnt.mall.domain.settlement.model.SettlementItem;
import com.lemnt.mall.domain.settlement.model.SettlementOrder;
import com.lemnt.trade.client.TradeOrderClient;
import com.lemnt.trade.client.bo.OrderCommentCreateBo;
import com.lemnt.trade.client.bo.OrderCreateBo;
import com.lemnt.trade.client.dto.OrderDTO;
import com.lemnt.trade.client.dto.OrderDeliveryDTO;
import com.lemnt.trade.client.dto.OrderItemDTO;

import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;

/** 只通过交易服务契约读写订单，不共享事务或 Mapper。 */
@Component
public class OrderGatewayImpl implements OrderGateway {
    @DubboReference(check = false, timeout = 5000, retries = 0)
    private TradeOrderClient client;

    @Override
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
                        order.promotions().stream()
                                .map(
                                        com.lemnt.mall.infrastructure.settlement.converter
                                                        .PromotionClientConverter
                                                ::to)
                                .toList());
        return from(
                "COMPLETE".equals(order.pricingSnapshotStatus())
                        ? client.createPriced(payload)
                        : client.create(payload));
    }

    @Override
    public Long createComment(CommentOrderRequest value) {
        return client.createComment(
                new OrderCommentCreateBo(
                        value.memberId(),
                        value.orderNo(),
                        value.skuId(),
                        value.orderItemId(),
                        value.score(),
                        value.content(),
                        value.descriptionScores(),
                        value.benefitScores(),
                        value.anonymous(),
                        value.picUrls()));
    }

    @Override
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
                        value.promotions().stream()
                                .map(
                                        com.lemnt.mall.infrastructure.settlement.converter
                                                        .PromotionClientConverter
                                                ::from)
                                .toList());
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
