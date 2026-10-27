package com.lemnt.trade.application.order.executor;

import com.lemnt.trade.application.order.service.OrderPersistenceService;
import com.lemnt.trade.domain.order.gateway.TradeOrderGateway;
import com.lemnt.trade.domain.order.model.TradeOrder;
import com.lemnt.trade.domain.outbox.gateway.TradeOutboxGateway;
import com.lemnt.trade.domain.payment.gateway.PaymentChannelCloseGateway;
import com.lemnt.trade.domain.payment.gateway.TradePaymentGateway;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import java.util.Set;
import java.util.UUID;

/**
 * 交易订单关闭命令执行器。
 */
@Component
@RequiredArgsConstructor
public class TradeOrderCloseCmdExe {
    private final OrderPersistenceService orderPersistenceService;
    private final TradeOrderGateway tradeOrderGateway;
    private final TradeOutboxGateway outbox;
    private final TradePaymentGateway payments;
    private final PaymentChannelCloseGateway channels;

    /**
     * Close the order and durably record the inventory release in one local transaction. No remote
     * inventory call is made before the transaction commits.
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean execute(String orderNo) {
        if (orderNo == null || orderNo.isBlank()) {
            throw new IllegalArgumentException("订单号不能为空");
        }
        TradeOrder order = orderPersistenceService.findByOrderNoForUpdate(orderNo);
        if (order == null) {
            throw new IllegalArgumentException("订单不存在");
        }
        if ("CLOSED".equals(order.status())) {
            return true;
        }
        if (!Set.of("PENDING_PAYMENT", "RESERVING").contains(order.status())) {
            return false;
        }
        for (var payment : payments.findByOrderNo(orderNo)) {
            if ("CLOSED".equals(payment.status())) {
                continue;
            }
            if (!"WAITING".equals(payment.status()) || !channels.closeUnpaid(payment)) {
                return false;
            }
            if (!payments.markClosed(payment.paymentNo())) {
                throw new IllegalStateException("支付单状态变化，请重试关单");
            }
        }
        if (!tradeOrderGateway.transitionStatus(orderNo, order.status(), "CLOSED")) {
            return false;
        }
        outbox.append(
                UUID.randomUUID().toString(),
                "TRADE_ORDER",
                orderNo,
                "TRADE_ORDER_CLOSED",
                "{\"orderNo\":\"" + orderNo + "\"}");
        return true;
    }
}
