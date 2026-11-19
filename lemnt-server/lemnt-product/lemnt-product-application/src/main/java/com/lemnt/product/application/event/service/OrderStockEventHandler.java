package com.lemnt.product.application.event.service;

import com.lemnt.common.client.event.CommerceEventEnvelope;
import com.lemnt.common.client.event.CommerceEventTopics;
import com.lemnt.product.application.product.service.ProductInventoryService;
import com.lemnt.product.domain.event.gateway.MessageInboxGateway;
import com.lemnt.product.domain.order.gateway.OrderEventFactsGateway;
import com.lemnt.product.domain.order.model.OrderStockLine;
import com.lemnt.product.domain.product.gateway.ProductSkuGateway;
import com.lemnt.product.domain.product.gateway.ProductSpuGateway;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * 商品库存按原预占台账核验支付，销量与库存确认在同一事务提交。
 */
@Component
@RequiredArgsConstructor
public class OrderStockEventHandler {
    private final MessageInboxGateway inbox;
    private final ProductInventoryService inventory;
    private final ProductSkuGateway productSkuGateway;
    private final ProductSpuGateway productSpuGateway;
    private final OrderEventFactsGateway orderFacts;

    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public void paid(CommerceEventEnvelope e) {
        e.requireType(CommerceEventTopics.ORDER_PAID);
        var v = orderFacts.paidFacts(e.payload());
        check(e, v.orderNo(), v.orderNo());
        if (inbox.has("product.order_paid.v1", CommerceEventTopics.ORDER_PAID, v.orderNo())) {
            // 旧版本的销量与 Inbox 同事务，重放时核验原载荷并保持历史预占行不变。
            begin(e, "product.order_paid.v1");
            return;
        }
        var stockLines = lines(v.lines());
        var parents = lockSalesSpus(stockLines);
        Map<Long, Integer> salesBySpu = new TreeMap<>();
        for (var line : stockLines) {
            if (inventory.confirmSale(v.orderNo(), line.skuId(), line.quantity())) {
                productSkuGateway.addSales(line.skuId(), line.quantity());
                var spuId = parents.get(line.skuId());
                if (spuId != null) {
                    salesBySpu.merge(spuId, line.quantity(), Math::addExact);
                }
            }
        }
        salesBySpu.forEach(productSpuGateway::addSales);
    }

    /**
     * 与后台编辑统一为SPU→SKU锁序；先按SPU编号加锁，再由调用方按SKU编号锁库存。
     * 历史独立SKU没有SPU归属，不补造销量主体，但仍正常确认原库存预占。
     */
    private Map<Long, Long> lockSalesSpus(List<OrderStockLine> stockLines) {
        Map<Long, Long> parents = new TreeMap<>();
        for (var line : stockLines) {
            var sku = productSkuGateway.catalogSku(line.skuId());
            if (sku == null) {
                throw new IllegalStateException("支付事实对应的商品SKU不存在");
            }
            if (sku.spuId() != null) {
                parents.put(line.skuId(), sku.spuId());
            }
        }
        for (Long spuId : parents.values().stream().distinct().sorted().toList()) {
            if (productSpuGateway.lockSpu(spuId) == null) {
                throw new IllegalStateException("支付事实对应的商品SPU不存在");
            }
        }
        return parents;
    }

    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public void closed(CommerceEventEnvelope e) {
        e.requireType(CommerceEventTopics.ORDER_CLOSED);
        var v = orderFacts.closedFacts(e.payload());
        check(e, v.orderNo(), v.orderNo());
        if (inbox.has("product.order_closed.v1", CommerceEventTopics.ORDER_CLOSED, v.orderNo())) {
            begin(e, "product.order_closed.v1");
            return;
        }
        var order = orderFacts.findClosedOrder(v.orderNo());
        var requested = lines(v.lines());
        if (order == null
                || !v.orderNo().equals(order.orderNo())
                || v.memberId() == null
                || !v.memberId().equals(order.memberId())
                || !requested.equals(lines(order.lines()))) {
            throw new IllegalStateException("关单事件与原订单预占事实不一致");
        }
        for (var line : requested) {
            inventory.cancelReservation(v.orderNo(), line.skuId(), line.quantity());
        }
    }

    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public void refunded(CommerceEventEnvelope e) {
        e.requireType(CommerceEventTopics.AFTER_SALE_COMPLETED);
        var v = orderFacts.afterSaleFacts(e.payload());
        check(e, v.orderNo(), v.afterSaleNo());
        if (!begin(e, "product.after_sale_completed.v1")) return;
        if (v.ruleVersion() != 1 || v.quantity() < 1 || v.skuId() == null)
            throw new IllegalArgumentException("售后冻结数量不合法");
        if (!inbox.has("product.order_paid.v1", CommerceEventTopics.ORDER_PAID, v.orderNo())) {
            var reservation = inventory.reservationStatus(v.orderNo(), v.skuId());
            if (reservation == null || !Boolean.TRUE.equals(reservation.salesRecorded())) {
                throw new IllegalStateException("原支付销量尚未入账，请重试");
            }
        }
        inventory.returnStock(v.orderNo(), v.afterSaleNo(), v.skuId(), v.quantity());
    }

    private boolean begin(CommerceEventEnvelope e, String key) {
        return inbox.begin(key, e.eventNo(), e.eventType(), e.businessKey(), e.payload());
    }

    private void check(CommerceEventEnvelope e, String no, String key) {
        if (!e.aggregateNo().equals(no) || !e.businessKey().equals(key))
            throw new IllegalArgumentException("事件订单或业务编号不一致");
    }

    private List<OrderStockLine> lines(List<OrderStockLine> values) {
        if (values == null
                || values.isEmpty()
                || values.stream().anyMatch(v -> v.skuId() == null || v.quantity() < 1)
                || values.stream().map(OrderStockLine::skuId).distinct().count() != values.size())
            throw new IllegalArgumentException("库存行不能为空或重复");
        return values.stream().sorted(Comparator.comparing(OrderStockLine::skuId)).toList();
    }
}
