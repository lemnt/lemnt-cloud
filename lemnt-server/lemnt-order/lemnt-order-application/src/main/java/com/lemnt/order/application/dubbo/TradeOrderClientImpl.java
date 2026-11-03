package com.lemnt.order.application.dubbo;

import com.lemnt.order.application.dubbo.converter.PromotionClientConverter;
import com.lemnt.order.application.order.cmd.OrderCommentCreateCmd;
import com.lemnt.order.application.order.service.OrderCommentService;
import com.lemnt.order.application.order.service.OrderPersistenceService;
import com.lemnt.order.application.order.service.OrderRemoteService;
import com.lemnt.order.application.order.service.OrderSubmissionService;
import com.lemnt.order.client.TradeOrderClient;
import com.lemnt.order.client.bo.OrderCommentCreateBo;
import com.lemnt.order.client.bo.OrderCreateBo;
import com.lemnt.order.client.dto.OrderDTO;
import com.lemnt.order.client.dto.OrderDeliveryDTO;
import com.lemnt.order.client.dto.OrderItemDTO;
import com.lemnt.order.domain.config.gateway.TradeConfigGateway;
import com.lemnt.order.domain.delivery.gateway.PickUpStoreGateway;
import com.lemnt.order.domain.delivery.model.PickUpStore;
import com.lemnt.order.domain.order.gateway.TradeOrderGateway;
import com.lemnt.order.domain.order.model.TradeOrder;
import com.lemnt.order.domain.order.model.TradeOrderDelivery;
import com.lemnt.order.domain.order.model.TradeOrderItem;

import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.concurrent.ThreadLocalRandom;

/** 接收商城确认后的缓存快照，交易服务不参与购物车或促销计价。 */
@DubboService
@RequiredArgsConstructor
public class TradeOrderClientImpl implements TradeOrderClient {
    private final OrderSubmissionService submissions;
    private final OrderPersistenceService orders;
    private final OrderRemoteService remotes;
    private final OrderCommentService comments;
    private final TradeOrderGateway orderGateway;
    private final TradeConfigGateway configs;
    private final PickUpStoreGateway pickUpStores;

    @Override
    public OrderDTO create(OrderCreateBo value) {
        if (value != null && (value.usePoint() < 0 || value.givePoint() < 0)) {
            throw new IllegalArgumentException("订单积分不能为负数");
        }
        if (value == null
                || value.orderNo() == null
                || !value.orderNo().matches("[A-Za-z0-9_-]{1,40}")
                || value.memberId() == null
                || value.memberId() <= 0
                || value.totalAmount() == null
                || value.totalAmount().signum() < 0
                || value.totalAmount().scale() > 2
                || (value.totalAmount().signum() == 0 && value.usePoint() <= 0)
                || value.items() == null
                || value.items().isEmpty()
                || value.items().size() > 100) {
            throw new IllegalArgumentException("订单快照不合法");
        }
        if (value != null
                && value.giveCouponTemplateCounts() != null
                && (value.giveCouponTemplateCounts().size() > 20
                        || value.giveCouponTemplateCounts().entrySet().stream()
                                .anyMatch(
                                        entry ->
                                                entry.getKey() == null
                                                        || entry.getKey() <= 0
                                                        || entry.getValue() == null
                                                        || entry.getValue() < 1
                                                        || entry.getValue() > 100))) {
            throw new IllegalArgumentException("订单赠券快照不合法");
        }
        var delivery = value.delivery();
        if (delivery != null) {
            int deliveryType = delivery.deliveryType() == null ? 1 : delivery.deliveryType();
            if (deliveryType == 1) {
                if (delivery.addressId() == null
                        || delivery.addressId() <= 0
                        || delivery.receiverName() == null
                        || delivery.receiverName().isBlank()
                        || delivery.receiverName().length() > 100
                        || delivery.receiverMobile() == null
                        || !delivery.receiverMobile().matches("[+0-9() -]{5,32}")
                        || delivery.receiverAreaId() == null
                        || delivery.receiverAreaId() <= 0
                        || delivery.receiverDetailAddress() == null
                        || delivery.receiverDetailAddress().isBlank()
                        || delivery.receiverDetailAddress().length() > 500
                        || delivery.deliveryPrice() == null
                        || delivery.deliveryPrice().signum() < 0
                        || delivery.deliveryPrice().scale() > 2) {
                    throw new IllegalArgumentException("订单收件快照不合法");
                }
            } else if (deliveryType == 2) {
                var config = configs.get();
                if (config == null || !Boolean.TRUE.equals(config.deliveryPickUpEnabled())) {
                    throw new IllegalArgumentException("自提功能未启用");
                }
                var store =
                        delivery.pickUpStoreId() == null
                                ? null
                                : pickUpStores.find(delivery.pickUpStoreId());
                if (store == null || !Integer.valueOf(0).equals(store.status())) {
                    throw new IllegalArgumentException("自提门店不存在或已关闭");
                }
                requireOpen(store);
                if (delivery.pickUpStoreId() == null
                        || delivery.pickUpStoreId() <= 0
                        || delivery.deliveryPrice() == null
                        || delivery.deliveryPrice().signum() != 0
                        || (delivery.receiverName() != null
                                && delivery.receiverName().length() > 100)
                        || (delivery.receiverMobile() != null
                                && !delivery.receiverMobile().matches("[+0-9() -]{5,32}"))
                        || (delivery.receiverDetailAddress() != null
                                && delivery.receiverDetailAddress().length() > 500)) {
                    throw new IllegalArgumentException("自提订单快照不合法");
                }
            } else {
                throw new IllegalArgumentException("配送方式不合法");
            }
        }
        for (var item : value.items()) {
            if (item == null
                    || item.productId() == null
                    || item.productId() <= 0
                    || item.deliveryPrice() == null
                    || item.deliveryPrice().signum() < 0
                    || item.deliveryPrice().scale() > 2
                    || item.quantity() == null
                    || item.quantity() < 1
                    || item.quantity() > 999
                    || item.unitPrice() == null
                    || item.unitPrice().signum() <= 0
                    || item.unitPrice().scale() > 2
                    || item.lineAmount() == null
                    || item.unitPrice()
                                    .multiply(BigDecimal.valueOf(item.quantity()))
                                    .compareTo(item.lineAmount())
                            != 0) {
                throw new IllegalArgumentException("订单明细快照不合法");
            }
        }
        // 商品元数据由商城报价生成，历史缺失字段允许为空，已有字段必须符合落库约束。
        for (var item : value.items()) {
            if ((item.spuId() != null && item.spuId() <= 0)
                    || (item.spuName() != null
                            && (item.spuName().isBlank() || item.spuName().length() > 200))
                    || (item.picUrl() != null && item.picUrl().length() > 1024)
                    || (item.specs() != null
                            && item.specs().stream()
                                    .anyMatch(
                                            property ->
                                                    property.specName() == null
                                                            || property.specName().isBlank()
                                                            || property.specName().length() > 64
                                                            || property.specValueName() == null
                                                            || property.specValueName().isBlank()
                                                            || property.specValueName().length()
                                                                    > 100
                                                            || ((property.specId() == null)
                                                                    != (property.specValueId()
                                                                            == null))
                                                            || (property.specId() != null
                                                                    && (property.specId() <= 0
                                                                            || property
                                                                                            .specValueId()
                                                                                    <= 0))))) {
                throw new IllegalArgumentException("商品规格快照不合法");
            }
        }
        for (var item : value.items()) {
            if (item.specs() != null
                    && (item.specs().size() > 10
                            || item.specs().stream()
                                            .map(property -> property.specName())
                                            .distinct()
                                            .count()
                                    != item.specs().size()
                            || item.specs().stream()
                                            .filter(property -> property.specId() != null)
                                            .map(property -> property.specId())
                                            .distinct()
                                            .count()
                                    != item.specs().stream()
                                            .filter(property -> property.specId() != null)
                                            .count())) {
                throw new IllegalArgumentException("商品规格重复或超过10项");
            }
        }
        if (value.items().stream().anyMatch(item -> item.payAmount() != null)) {
            if (value.items().stream()
                            .anyMatch(
                                    item ->
                                            item.payAmount() == null
                                                    || item.payAmount().signum() < 0
                                                    || item.payAmount().scale() > 2
                                                    || item.payAmount()
                                                                    .compareTo(
                                                                            item.lineAmount()
                                                                                    .add(
                                                                                            item
                                                                                                    .deliveryPrice()))
                                                            > 0)
                    || value.items().stream()
                                    .map(OrderItemDTO::payAmount)
                                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                                    .compareTo(value.totalAmount())
                            != 0) {
                throw new IllegalArgumentException("商品行实付与订单总金额不一致");
            }
        }
        var deliveryTotal =
                value.items().stream()
                        .map(OrderItemDTO::deliveryPrice)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (deliveryTotal.compareTo(delivery == null ? BigDecimal.ZERO : delivery.deliveryPrice())
                        != 0
                || (delivery != null
                        && value.items().stream().anyMatch(item -> item.payAmount() == null))) {
            throw new IllegalArgumentException("订单运费分摊不一致");
        }
        String verifyCode =
                delivery != null && Integer.valueOf(2).equals(delivery.deliveryType())
                        ? verifyCode()
                        : null;
        var order =
                new TradeOrder(
                        null,
                        value.orderNo(),
                        value.memberId(),
                        value.totalAmount(),
                        "RESERVING",
                        value.items().stream()
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
                                                        item.specs() == null
                                                                ? null
                                                                : item.specs().stream()
                                                                        .map(
                                                                                snapshotProperty ->
                                                                                        new TradeOrderItem
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
                        null,
                        value.usePoint(),
                        value.givePoint(),
                        fromDelivery(value.delivery(), verifyCode),
                        null,
                        null,
                        value.giveCouponTemplateCounts(),
                        value.originalAmount(),
                        value.discountAmount(),
                        value.deliveryOriginalPrice(),
                        value.deliveryDiscountPrice(),
                        value.adjustmentAmount(),
                        value.pricingSnapshotStatus(),
                        value.pricingSnapshotVersion(),
                        value.promotions().stream().map(PromotionClientConverter::from).toList());
        try {
            submissions.create(order);
        } catch (DuplicateKeyException exception) {
            var previous = orders.findByOrderNo(order.orderNo());
            if (previous == null) {
                throw exception;
            }
            submissions.requireSame(previous, order);
        }
        remotes.reserve(order.orderNo());
        return to(orders.findByOrderNo(order.orderNo()));
    }

    @Override
    public Long createComment(OrderCommentCreateBo value) {
        return comments.create(
                new OrderCommentCreateCmd(
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
    public OrderDTO getByOrderNo(String orderNo) {
        return to(orders.findByOrderNo(orderNo));
    }

    private OrderDTO to(TradeOrder value) {
        return value == null
                ? null
                : new OrderDTO(
                        value.id(),
                        value.orderNo(),
                        value.memberId(),
                        value.totalAmount(),
                        value.status(),
                        value.items().stream()
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
                        toDelivery(value.delivery()),
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
                        value.promotions().stream().map(PromotionClientConverter::to).toList());
    }

    private TradeOrderDelivery fromDelivery(OrderDeliveryDTO value, String verifyCode) {
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
                        verifyCode);
    }

    /** 门店营业时间按 HH:mm:ss 解析；关门早于开门表示跨夜营业。 */
    private void requireOpen(PickUpStore store) {
        try {
            LocalTime now = LocalTime.now();
            LocalTime open = LocalTime.parse(store.openingTime());
            LocalTime close = LocalTime.parse(store.closingTime());
            boolean openNow =
                    open.equals(close)
                            || (open.isBefore(close)
                                    ? !now.isBefore(open) && now.isBefore(close)
                                    : !now.isBefore(open) || now.isBefore(close));
            if (!openNow) {
                throw new IllegalArgumentException("自提门店当前未营业");
            }
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException("自提门店营业时间配置不合法");
        }
    }

    /** 8 位核销码，与数据库唯一索引配合；重试时订单内容比较会忽略核销码。 */
    private String verifyCode() {
        for (int attempt = 0; attempt < 10; attempt++) {
            String code = String.format("%08d", ThreadLocalRandom.current().nextInt(100_000_000));
            if (orderGateway.findHeaderByPickUpCode(code) == null) {
                return code;
            }
        }
        throw new IllegalStateException("核销码生成失败，请重试");
    }

    private OrderDeliveryDTO toDelivery(TradeOrderDelivery value) {
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

    /** 完整报价保持原建单编排，在内部持久化事务校验所有优惠及权益分摊。 */
    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public OrderDTO createPriced(OrderCreateBo value) {
        if (value == null || !"COMPLETE".equals(value.pricingSnapshotStatus())) {
            throw new IllegalArgumentException("完整报价快照标记缺失");
        }
        return create(value);
    }
}
