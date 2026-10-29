package com.lemnt.trade.application.dubbo;

import com.lemnt.trade.application.order.service.OrderPersistenceService;
import com.lemnt.trade.application.order.service.OrderRemoteService;
import com.lemnt.trade.application.order.service.OrderSubmissionService;
import com.lemnt.trade.client.TradeOrderClient;
import com.lemnt.trade.client.bo.OrderCreateBo;
import com.lemnt.trade.client.dto.OrderDTO;
import com.lemnt.trade.client.dto.OrderDeliveryDTO;
import com.lemnt.trade.client.dto.OrderItemDTO;
import com.lemnt.trade.domain.order.model.TradeOrder;
import com.lemnt.trade.domain.order.model.TradeOrderDelivery;
import com.lemnt.trade.domain.order.model.TradeOrderItem;

import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.dao.DuplicateKeyException;

import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

/** 接收商城确认后的缓存快照，交易服务不参与购物车或促销计价。 */
@DubboService
@RequiredArgsConstructor
public class TradeOrderClientImpl implements TradeOrderClient {
    private final OrderSubmissionService submissions;
    private final OrderPersistenceService orders;
    private final OrderRemoteService remotes;
    private final com.lemnt.trade.application.order.service.OrderCommentService comments;

    @Override
    public OrderDTO create(OrderCreateBo value) {
        if (value != null && (value.usePoint() < 0 || value.givePoint() < 0)
        ) {
            throw new IllegalArgumentException("订单积分不能为负数");
        }
        if (value == null || value.orderNo() == null || !value.orderNo().matches("[A-Za-z0-9_-]{1,40}")
                || value.memberId() == null || value.memberId() <= 0 || value.totalAmount() == null
                || value.totalAmount().signum() <= 0 || value.totalAmount().scale() > 2
                || value.items() == null || value.items().isEmpty() || value.items().size() > 100
        ) {
            throw new IllegalArgumentException("订单快照不合法");
        }
        var delivery = value.delivery();
        if (delivery != null && (delivery.addressId() == null || delivery.addressId() <= 0
                || !Integer.valueOf(1).equals(delivery.deliveryType())
                || delivery.receiverName() == null || delivery.receiverName().isBlank() || delivery.receiverName().length() > 100
                || delivery.receiverMobile() == null || !delivery.receiverMobile().matches("[+0-9() -]{5,32}")
                || delivery.receiverAreaId() == null || delivery.receiverAreaId() <= 0
                || delivery.receiverDetailAddress() == null || delivery.receiverDetailAddress().isBlank()
                || delivery.receiverDetailAddress().length() > 500 || delivery.deliveryPrice() == null
                || delivery.deliveryPrice().signum() < 0 || delivery.deliveryPrice().scale() > 2)
        ) {
            throw new IllegalArgumentException("订单收件快照不合法");
        }
        for (var item : value.items()
        ) {
            if (item == null || item.productId() == null || item.productId() <= 0
                    || item.deliveryPrice() == null || item.deliveryPrice().signum() < 0
                    || item.deliveryPrice().scale() > 2
                    || item.quantity() == null || item.quantity() < 1 || item.quantity() > 999
                    || item.unitPrice() == null || item.unitPrice().signum() <= 0
                    || item.unitPrice().scale() > 2 || item.lineAmount() == null
                    || item.unitPrice().multiply(BigDecimal.valueOf(item.quantity()))
                            .compareTo(item.lineAmount()) != 0
            ) {
                throw new IllegalArgumentException("订单明细快照不合法");
            }
        }
        // 商品元数据由商城报价生成，历史缺失字段允许为空，已有字段必须符合落库约束。
        for (var item : value.items()) {
            if ((item.spuId() != null && item.spuId() <= 0)
                    || (item.spuName() != null && (item.spuName().isBlank() || item.spuName().length() > 200))
                    || (item.picUrl() != null && item.picUrl().length() > 1024)
                    || (item.properties() != null && item.properties().stream().anyMatch(property ->
                            property.propertyName() == null || property.propertyName().isBlank() || property.propertyName().length() > 64
                                    || property.valueName() == null || property.valueName().isBlank() || property.valueName().length() > 100
                                    || ((property.propertyId() == null) != (property.valueId() == null))
                                    || (property.propertyId() != null && (property.propertyId() <= 0 || property.valueId() <= 0))))) {
                throw new IllegalArgumentException("商品规格快照不合法");
            }
        }
        for (var item : value.items()) {
            if (item.properties() != null && (item.properties().size() > 10
                    || item.properties().stream().map(property -> property.propertyName()).distinct().count() != item.properties().size()
                    || item.properties().stream().filter(property -> property.propertyId() != null)
                            .map(property -> property.propertyId()).distinct().count()
                            != item.properties().stream().filter(property -> property.propertyId() != null).count())) {
                throw new IllegalArgumentException("商品规格重复或超过10项");
            }
        }
        if (value.items().stream().anyMatch(item -> item.payAmount() != null)
        ) {
            if (value.items().stream().anyMatch(item -> item.payAmount() == null
                    || item.payAmount().signum() < 0 || item.payAmount().scale() > 2
                    || item.payAmount().compareTo(item.lineAmount().add(item.deliveryPrice())) > 0)
                    || value.items().stream().map(OrderItemDTO::payAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add).compareTo(value.totalAmount()) != 0
            ) {
                throw new IllegalArgumentException("商品行实付与订单总金额不一致");
            }
        }
        var deliveryTotal = value.items().stream().map(OrderItemDTO::deliveryPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (deliveryTotal.compareTo(delivery == null ? BigDecimal.ZERO : delivery.deliveryPrice()) != 0
                || (delivery != null && value.items().stream().anyMatch(item -> item.payAmount() == null))
        ) {
            throw new IllegalArgumentException("订单运费分摊不一致");
        }
        var order = new TradeOrder(null, value.orderNo(), value.memberId(), value.totalAmount(),
                "RESERVING", value.items().stream().map(item -> new TradeOrderItem(item.productId(),
                item.productCode(),
                item.productName(),
                item.unitPrice(),
                item.quantity(),
                item.lineAmount(),
                item.payAmount(),
                item.deliveryPrice(),
                item.spuId(),
                item.spuName(),
                item.properties() == null ? null : item.properties().stream().map(snapshotProperty ->
                        new TradeOrderItem.Property(snapshotProperty.propertyId(), snapshotProperty.propertyName(),
                                snapshotProperty.valueId(), snapshotProperty.valueName())).toList(),
                item.picUrl())).toList(), null, value.usePoint(), value.givePoint(), fromDelivery(value.delivery()));
        try {
            submissions.create(order);
        } catch (DuplicateKeyException exception
        ) {
            var previous = orders.findByOrderNo(order.orderNo());
            if (previous == null
            ) {
                throw exception;
            }
            submissions.requireSame(previous, order);
        }
        remotes.reserve(order.orderNo());
        return to(orders.findByOrderNo(order.orderNo()));
    }

    @Override
    public Long createComment(com.lemnt.trade.client.bo.OrderCommentCreateBo value) {
        return comments.create(new com.lemnt.trade.application.order.cmd.OrderCommentCreateCmd(value.memberId(),
                value.orderNo(), value.skuId(), value.orderItemId(), value.score(), value.content(),
                value.descriptionScores(), value.benefitScores(), value.anonymous(), value.picUrls()));
    }

    @Override
    public OrderDTO getByOrderNo(String orderNo) {
        return to(orders.findByOrderNo(orderNo));
    }

    private OrderDTO to(TradeOrder value) {
        return value == null ? null : new OrderDTO(value.id(), value.orderNo(), value.memberId(),
                value.totalAmount(), value.status(), value.items().stream().map(item ->
                        new OrderItemDTO(item.productId(),
                item.productCode(),
                item.productName(),
                item.unitPrice(),
                item.quantity(),
                item.lineAmount(),
                item.payAmount(),
                item.deliveryPrice(),
                item.spuId(),
                item.spuName(),
                item.properties() == null ? null : item.properties().stream().map(snapshotProperty ->
                        new OrderItemDTO.Property(snapshotProperty.propertyId(), snapshotProperty.propertyName(),
                                snapshotProperty.valueId(), snapshotProperty.valueName())).toList(),
                item.picUrl(), item.orderItemId(), item.commentStatus())).toList(), value.createTime(), value.usePoint(), value.givePoint(), toDelivery(value.delivery()), value.commentStatus(), value.finishTime());
    }

    private TradeOrderDelivery fromDelivery(
            OrderDeliveryDTO value
    ) {
        return value == null ? null : new TradeOrderDelivery(value.addressId(),
                value.deliveryType(), value.receiverName(), value.receiverMobile(), value.receiverAreaId(),
                value.receiverDetailAddress(), value.deliveryPrice());
    }

    private OrderDeliveryDTO toDelivery(
            TradeOrderDelivery value
    ) {
        return value == null ? null : new OrderDeliveryDTO(value.addressId(),
                value.deliveryType(), value.receiverName(), value.receiverMobile(), value.receiverAreaId(),
                value.receiverDetailAddress(), value.deliveryPrice());
    }
}
