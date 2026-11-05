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
 * 商品库存只处理已核验的订单事件；重复、乱序或失败都不能提前提交消费凭证。
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
        if (!begin(e, "product.order_paid.v1")) return;
        var stockLines = lines(v.lines());
        var salesBySpu = lockSalesSpus(stockLines);
        for (var line : stockLines) {
            var original = inventory.reservationStatus(v.orderNo(), line.skuId());
            if (original == null || original.quantity() != line.quantity())
                throw new IllegalStateException("原预占数量与支付事实不一致");
            inventory.confirmStock(v.orderNo(), line.skuId());
        }
        // 库存确认、销量和消费凭证同事务；每个SPU汇总累加一次，失败整体回滚。
        salesBySpu.forEach(productSpuGateway::addSales);
    }

    /**
     * 与后台编辑统一为SPU→SKU锁序；先按SPU编号加锁，再由调用方按SKU编号锁库存。
     * 历史独立SKU没有SPU归属，不补造销量主体，但仍正常确认原库存预占。
     */
    private Map<Long, Integer> lockSalesSpus(List<OrderStockLine> stockLines) {
        Map<Long, Integer> salesBySpu = new TreeMap<>();
        for (var line : stockLines) {
            var sku = productSkuGateway.catalogSku(line.skuId());
            if (sku == null) {
                throw new IllegalStateException("支付事实对应的商品SKU不存在");
            }
            if (sku.spuId() != null) {
                salesBySpu.merge(sku.spuId(), line.quantity(), Math::addExact);
            }
        }
        for (Long spuId : salesBySpu.keySet()) {
            if (productSpuGateway.lockSpu(spuId) == null) {
                throw new IllegalStateException("支付事实对应的商品SPU不存在");
            }
        }
        return salesBySpu;
    }

    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public void closed(CommerceEventEnvelope e) {
        e.requireType(CommerceEventTopics.ORDER_CLOSED);
        var v = orderFacts.closedFacts(e.payload());
        check(e, v.orderNo(), v.orderNo());
        if (!begin(e, "product.order_closed.v1")) return;
        for (var line : lines(v.lines()))
            inventory.cancelReservation(v.orderNo(), line.skuId(), line.quantity());
    }

    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public void refunded(CommerceEventEnvelope e) {
        e.requireType(CommerceEventTopics.AFTER_SALE_COMPLETED);
        var v = orderFacts.afterSaleFacts(e.payload());
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

    private List<OrderStockLine> lines(List<OrderStockLine> values) {
        if (values == null
                || values.isEmpty()
                || values.stream().anyMatch(v -> v.skuId() == null || v.quantity() < 1)
                || values.stream().map(OrderStockLine::skuId).distinct().count() != values.size())
            throw new IllegalArgumentException("库存行不能为空或重复");
        return values.stream().sorted(Comparator.comparing(OrderStockLine::skuId)).toList();
    }
}
