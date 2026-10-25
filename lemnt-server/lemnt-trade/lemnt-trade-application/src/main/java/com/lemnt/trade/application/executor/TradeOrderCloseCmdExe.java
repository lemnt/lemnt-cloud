package com.lemnt.trade.application.executor;

import com.lemnt.trade.domain.gateway.TradeOrderGateway;
import com.lemnt.trade.domain.gateway.TradeOutboxGateway;
import com.lemnt.trade.domain.model.TradeOrder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TradeOrderCloseCmdExe {
    private final TradeOrderGateway orders;
    private final TradeOutboxGateway outbox;

    /**
     * Close the order and durably record the inventory release in one local transaction.
     * No remote inventory call is made before the transaction commits.
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean execute(String orderNo) {
        if (orderNo == null || orderNo.isBlank()) throw new IllegalArgumentException("订单号不能为空");
        TradeOrder order = orders.findByOrderNo(orderNo);
        if (order == null) throw new IllegalArgumentException("订单不存在");
        if ("CLOSED".equals(order.status())) return true;
        if (!"PENDING_PAYMENT".equals(order.status())) return false;
        if (!orders.transitionStatus(orderNo, "PENDING_PAYMENT", "CLOSED")) return false;
        outbox.append(UUID.randomUUID().toString(), "TRADE_ORDER", orderNo, "TRADE_ORDER_CLOSED",
                "{\"orderNo\":\"" + orderNo + "\"}");
        return true;
    }
}
