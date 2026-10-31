package com.lemnt.trade.application.fulfillment.service;

import com.lemnt.trade.application.order.service.OrderLogService;
import com.lemnt.trade.domain.order.model.TradeOperator;


import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.trade.application.fulfillment.cmd.ShipmentCreateCmd;
import com.lemnt.trade.application.order.service.OrderPersistenceService;
import com.lemnt.trade.domain.delivery.gateway.DeliveryExpressGateway;
import com.lemnt.trade.domain.fulfillment.gateway.InventoryConfirmationGateway;
import com.lemnt.trade.domain.aftersale.gateway.AfterSaleGateway;
import com.lemnt.trade.domain.fulfillment.gateway.ShipmentGateway;
import com.lemnt.trade.domain.fulfillment.model.Shipment;
import com.lemnt.trade.domain.order.gateway.TradeOrderGateway;
import com.lemnt.trade.domain.order.model.OrderRules;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import java.util.Set;

/**
 * 订单履约服务。
 */
@Component
@RequiredArgsConstructor
public class OrderFulfillmentService {
    private final InventoryConfirmationGateway inventoryConfirmationGateway;
    private final OrderPersistenceService orderPersistenceService;
    private final AfterSaleGateway refundRequestGateway;
    private final ShipmentGateway shipmentGateway;
    private final TradeOrderGateway tradeOrderGateway;
    private final DeliveryExpressGateway deliveryExpressGateway;
    private final OrderLogService logs;
    private String fingerprint(String... values) {
        StringBuilder canonical = new StringBuilder();
        for (var value : values
        ) {
            canonical.append(value.length()).append(':').append(value);
        }
        return OrderRules.fingerprint(canonical.toString());
    }

    /**
     * 发货。
     */
    @Transactional(rollbackFor = Exception.class)
    public Shipment ship(ShipmentCreateCmd cmd) {
        if (cmd == null
        ) {
            throw new IllegalArgumentException("发货参数不能为空");
        }
        String orderNo = OrderRules.text(cmd.orderNo(), 40);
        String requestNo = OrderRules.text(cmd.requestNo(), 64);
        String carrier = OrderRules.text(cmd.carrierCode(), 40);
        String tracking = OrderRules.text(cmd.trackingNo(), 100);
        var order = orderPersistenceService.findByOrderNoForUpdate(orderNo);
        if (order == null
        ) {
            throw new IllegalArgumentException("订单不存在");
        }
        var delivery = order.delivery();
        String name = OrderRules.text(delivery == null ? cmd.recipientName() : delivery.receiverName(), 100);
        String phone = OrderRules.text(delivery == null ? cmd.phone() : delivery.receiverMobile(), 32);
        if (!phone.matches("[+0-9() -]{5,32}")
        ) {
            throw new IllegalArgumentException("联系电话不合法");
        }
        String address = OrderRules.text(delivery == null ? cmd.address() : delivery.receiverDetailAddress(), 500);
        String hash = fingerprint(carrier, tracking, name, phone, address);
        var previous = shipmentGateway.shipment(orderNo);
        if (previous != null
        ) {
            if (!Set.of("SHIPPED", "COMPLETED").contains(order.status())
            ) {
                throw new IllegalStateException("物流记录与订单状态不一致");
            }
            if (!requestNo.equals(previous.requestNo()) || !hash.equals(previous.fingerprint())
            ) {
                throw new IllegalStateException("订单已发货，请查询原记录，不能重复或更改物流");
            }
            return previous;
        }
        if (!"PAID".equals(order.status()) || !inventoryConfirmationGateway.inventoryConfirmed(orderNo)
        ) {
            throw new IllegalStateException("仅已支付且库存确认完成的订单可以发货");
        }
        if (refundRequestGateway.activeRefund(orderNo)
        ) {
            throw new IllegalStateException("订单有待处理退款申请，不能发货");
        }
        var company = deliveryExpressGateway.byCode(carrier);
        if (company == null || !Integer.valueOf(0).equals(company.status())
        ) {
            throw new ServiceException("快递公司不存在或已停用", 400);
        }
        shipmentGateway.saveShipment(
                new Shipment(
                orderNo,
                requestNo,
                hash,
                carrier,
                tracking,
                name,
                phone,
                address,
                tradeOrderGateway.databaseNow(),
                null));
        if (!tradeOrderGateway.transitionStatus(orderNo, "PAID", "SHIPPED")
        ) {
            throw new IllegalStateException("订单状态冲突");
        }
        logs.record(order, "PAID", "SHIPPED", 20, cmd.operator(), "已发货，快递公司：" + carrier + "，快递单号：" + tracking);
        return shipmentGateway.shipment(orderNo);
    }

    /**
     * 接收。
     */
    @Transactional(rollbackFor = Exception.class)
    public Shipment receive(String input) {
        return receive(input, TradeOperator.system());
    }

    /** 会员确认本人收货，系统任务可自动收货。 */
    @Transactional(rollbackFor = Exception.class)
    public Shipment receive(String input, TradeOperator operator) {
        String orderNo = OrderRules.text(input, 40);
        var order = orderPersistenceService.findByOrderNoForUpdate(orderNo);
        var shipment = shipmentGateway.shipment(orderNo);
        if (order == null || shipment == null
        ) {
            throw new IllegalArgumentException("订单或物流不存在");
        }
        if (operator.userType() == 1 && !operator.userId().equals(order.memberId())) {
            throw new ServiceException("订单不存在", 400);
        }
        if ("COMPLETED".equals(order.status())
        ) {
            if (shipment.receivedAt() == null
            ) {
                throw new IllegalStateException("订单收货记录不一致");
            }
            return shipment;
        }
        if (!"SHIPPED".equals(order.status()) || refundRequestGateway.activeRefund(orderNo)
        ) {
            throw new IllegalStateException("订单状态不允许确认收货");
        }
        var receivedAt = tradeOrderGateway.databaseNow();
        if (!shipmentGateway.receive(orderNo, receivedAt)
                || !tradeOrderGateway.completeReceipt(orderNo, receivedAt)
        ) {
            throw new IllegalStateException("收货状态冲突");
        }
        logs.record(order, "SHIPPED", "COMPLETED", operator.userType() == 1 ? 30 : 31, operator,
                operator.userType() == 1 ? "用户已收货" : "系统自动确认收货");
        return shipmentGateway.shipment(orderNo);
    }

}
