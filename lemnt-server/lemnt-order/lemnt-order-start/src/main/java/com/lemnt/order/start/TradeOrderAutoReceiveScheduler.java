package com.lemnt.order.start;

import com.lemnt.order.application.fulfillment.service.OrderFulfillmentService;
import com.lemnt.order.domain.fulfillment.gateway.ShipmentGateway;
import com.lemnt.order.domain.order.gateway.TradeOrderGateway;
import com.lemnt.order.domain.order.model.TradeOperator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * 超期未收货订单自动确认收货，逐单隔离失败。
 */
@Component
@ConditionalOnProperty(name = "lemnt.trade.auto-receive.enabled", havingValue = "true")
public class TradeOrderAutoReceiveScheduler {
    private static final Logger LOG = LoggerFactory.getLogger(TradeOrderAutoReceiveScheduler.class);
    private final ShipmentGateway shipments;
    private final TradeOrderGateway orders;
    private final OrderFulfillmentService fulfillment;
    private final Duration expiry;

    public TradeOrderAutoReceiveScheduler(
        ShipmentGateway shipments,
        TradeOrderGateway orders,
        OrderFulfillmentService fulfillment,
        @Value("${lemnt.trade.auto-receive.expire:7d}") Duration expiry) {
        if (expiry == null || expiry.isNegative() || expiry.isZero()) {
            throw new IllegalArgumentException("自动收货期限必须为正");
        }
        this.shipments = shipments;
        this.orders = orders;
        this.fulfillment = fulfillment;
        this.expiry = expiry;
    }

    @Scheduled(fixedDelayString = "${lemnt.trade.auto-receive.delay-ms:60000}")
    public void execute() {
        var cutoff = orders.databaseNow().minus(expiry);
        for (String orderNo : shipments.shippedBefore(cutoff, 50)) {
            try {
                fulfillment.receive(orderNo, TradeOperator.system());
            } catch (RuntimeException error) {
                LOG.error("自动确认收货失败，订单 {}", orderNo, error);
            }
        }
    }
}
