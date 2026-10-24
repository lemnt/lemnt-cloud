package com.lemnt.trade.application.executor;

import com.lemnt.trade.domain.gateway.ProductCatalogGateway;
import com.lemnt.trade.domain.gateway.TradeOrderGateway;
import com.lemnt.trade.domain.model.TradeOrder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class TradeOrderCloseCmdExe {
    private final TradeOrderGateway orders;
    private final ProductCatalogGateway products;

    /**
     * Can be called by a scheduler for an expired order or an operator retry.
     * The conditional status transition is the concurrency guard.
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean execute(String orderNo) {
        TradeOrder order = orders.findByOrderNo(orderNo);
        if (order == null) throw new IllegalArgumentException("订单不存在");
        if ("CLOSED".equals(order.status())) return true;
        if (!"PENDING_PAYMENT".equals(order.status())) return false;
        if (!orders.transitionStatus(orderNo, "PENDING_PAYMENT", "CLOSED")) return false;

        // Stock releases are idempotent per order/product. If a remote call fails,
        // the durable outbox/reconciliation worker must retry the release.
        for (var item : order.items()) {
            products.releaseStock(orderNo, item.productId());
        }
        return true;
    }
}