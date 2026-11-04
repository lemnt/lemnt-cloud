package com.lemnt.product.application.event.service;

import com.lemnt.common.client.event.CommerceEventEnvelope;
import com.lemnt.common.client.event.CommerceEventTopics;
import com.lemnt.common.core.utils.JsonUtils;
import com.lemnt.order.client.dto.OrderStockLineDTO;
import com.lemnt.order.client.event.AfterSaleCompletedEvent;
import com.lemnt.order.client.event.OrderClosedEvent;
import com.lemnt.order.client.event.OrderPaidEvent;
import com.lemnt.product.application.product.service.ProductInventoryService;
import com.lemnt.product.domain.event.gateway.MessageInboxGateway;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import java.util.Comparator;
import java.util.List;

/**
 * 商品库存只处理已核验的订单事件；重复、乱序或失败都不能提前提交消费凭证。
 */
@Component
@RequiredArgsConstructor
public class OrderStockEventHandler {
    private final MessageInboxGateway inbox;
    private final ProductInventoryService inventory;

    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public void paid(CommerceEventEnvelope e) {
        e.requireType(CommerceEventTopics.ORDER_PAID);
        var v = JsonUtils.toBean(e.payload(), OrderPaidEvent.class);
        if (v == null) throw new IllegalArgumentException("订单事实不能为空");
        check(e, v.orderNo(), v.orderNo());
        if (!begin(e, "product.order_paid.v1")) return;
        for (var line : lines(v.items())) {
            var original = inventory.reservationStatus(v.orderNo(), line.skuId());
            if (original == null || original.quantity() != line.quantity())
                throw new IllegalStateException("原预占数量与支付事实不一致");
            inventory.confirmStock(v.orderNo(), line.skuId());
        }
    }

    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public void closed(CommerceEventEnvelope e) {
        e.requireType(CommerceEventTopics.ORDER_CLOSED);
        var v = JsonUtils.toBean(e.payload(), OrderClosedEvent.class);
        if (v == null) throw new IllegalArgumentException("关单事实不能为空");
        check(e, v.orderNo(), v.orderNo());
        if (!begin(e, "product.order_closed.v1")) return;
        for (var line : lines(v.items()))
            inventory.cancelReservation(v.orderNo(), line.skuId(), line.quantity());
    }

    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public void refunded(CommerceEventEnvelope e) {
        e.requireType(CommerceEventTopics.AFTER_SALE_COMPLETED);
        var v = JsonUtils.toBean(e.payload(), AfterSaleCompletedEvent.class);
        if (v == null) throw new IllegalArgumentException("售后事实不能为空");
        check(e, v.orderNo(), v.afterSaleNo());
        if (!begin(e, "product.after_sale_completed.v1")) return;
        if (v.ruleVersion() != 1 || v.quantity() < 1 || v.skuId() == null)
            throw new IllegalArgumentException("售后冻结数量不合法");
        if (!inbox.has("product.order_paid.v1", CommerceEventTopics.ORDER_PAID, v.orderNo()))
            throw new IllegalStateException("原支付库存尚未确认，请重试");
        inventory.returnStock(v.orderNo(), v.afterSaleNo(), v.skuId(), v.quantity());
    }

    private boolean begin(CommerceEventEnvelope e, String key) {
        return inbox.begin(key, e.eventNo(), e.eventType(), e.businessKey(), e.payload());
    }

    private void check(CommerceEventEnvelope e, String no, String key) {
        if (!e.aggregateNo().equals(no) || !e.businessKey().equals(key))
            throw new IllegalArgumentException("事件订单或业务编号不一致");
    }

    private List<OrderStockLineDTO> lines(List<OrderStockLineDTO> values) {
        if (values == null
            || values.isEmpty()
            || values.stream().anyMatch(v -> v.skuId() == null || v.quantity() < 1)
            || values.stream().map(OrderStockLineDTO::skuId).distinct().count()
            != values.size()) throw new IllegalArgumentException("库存行不能为空或重复");
        return values.stream().sorted(Comparator.comparing(OrderStockLineDTO::skuId)).toList();
    }
}
