package com.lemnt.trade.application.fulfillment.service;

import com.lemnt.trade.application.fulfillment.cmd.*;
import com.lemnt.trade.domain.fulfillment.gateway.FulfillmentGateway;
import com.lemnt.trade.domain.fulfillment.model.*;
import com.lemnt.trade.domain.checkout.model.CheckoutRules;
import com.lemnt.trade.domain.gateway.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import java.util.*;

@Component
@RequiredArgsConstructor
public class OrderFulfillmentService {
    private final TradeOrderGateway orders;
    private final TradePaymentGateway payments;
    private final FulfillmentGateway fulfillment;

    private String fingerprint(String... values) {
        StringBuilder canonical = new StringBuilder();
        for (var value : values) canonical.append(value.length()).append(':').append(value);
        return CheckoutRules.fingerprint(canonical.toString());
    }

    @Transactional(rollbackFor = Exception.class)
    public Shipment ship(ShipmentCreateCmd cmd) {
        if (cmd == null) throw new IllegalArgumentException("发货参数不能为空");
        String orderNo = CheckoutRules.text(cmd.orderNo(), 40);
        String requestNo = CheckoutRules.text(cmd.requestNo(), 64);
        String carrier = CheckoutRules.text(cmd.carrierCode(), 40);
        String tracking = CheckoutRules.text(cmd.trackingNo(), 100);
        String name = CheckoutRules.text(cmd.recipientName(), 100);
        String phone = CheckoutRules.text(cmd.phone(), 32);
        if (!phone.matches("[+0-9() -]{5,32}")) throw new IllegalArgumentException("联系电话不合法");
        String address = CheckoutRules.text(cmd.address(), 500);
        String hash = fingerprint(carrier, tracking, name, phone, address);
        var order = orders.findByOrderNoForUpdate(orderNo);
        if (order == null) throw new IllegalArgumentException("订单不存在");
        var previous = fulfillment.shipment(orderNo);
        if (previous != null) {
            if (!Set.of("SHIPPED", "COMPLETED").contains(order.status()))
                throw new IllegalStateException("物流记录与订单状态不一致");
            if (!requestNo.equals(previous.requestNo()) || !hash.equals(previous.fingerprint()))
                throw new IllegalStateException("订单已发货，请查询原记录，不能重复或更改物流");
            return previous;
        }
        if (!"PAID".equals(order.status()) || !fulfillment.inventoryConfirmed(orderNo))
            throw new IllegalStateException("仅已支付且库存确认完成的订单可以发货");
        if (fulfillment.activeRefund(orderNo)) throw new IllegalStateException("订单有待处理退款申请，不能发货");
        fulfillment.saveShipment(
                new Shipment(
                        orderNo,
                        requestNo,
                        hash,
                        carrier,
                        tracking,
                        name,
                        phone,
                        address,
                        orders.databaseNow(),
                        null));
        if (!orders.transitionStatus(orderNo, "PAID", "SHIPPED"))
            throw new IllegalStateException("订单状态冲突");
        return fulfillment.shipment(orderNo);
    }

    @Transactional(rollbackFor = Exception.class)
    public Shipment receive(String input) {
        String orderNo = CheckoutRules.text(input, 40);
        var order = orders.findByOrderNoForUpdate(orderNo);
        var shipment = fulfillment.shipment(orderNo);
        if (order == null || shipment == null) throw new IllegalArgumentException("订单或物流不存在");
        if ("COMPLETED".equals(order.status())) {
            if (shipment.receivedAt() == null) throw new IllegalStateException("订单收货记录不一致");
            return shipment;
        }
        if (!"SHIPPED".equals(order.status()) || fulfillment.activeRefund(orderNo))
            throw new IllegalStateException("订单状态不允许确认收货");
        if (!fulfillment.receive(orderNo, orders.databaseNow())
                || !orders.transitionStatus(orderNo, "SHIPPED", "COMPLETED"))
            throw new IllegalStateException("收货状态冲突");
        return fulfillment.shipment(orderNo);
    }

    @Transactional(rollbackFor = Exception.class)
    public RefundRequest requestRefund(RefundRequestCmd cmd) {
        if (cmd == null) throw new IllegalArgumentException("退款申请参数不能为空");
        String orderNo = CheckoutRules.text(cmd.orderNo(), 40);
        String key = CheckoutRules.text(cmd.requestNo(), 64);
        String reason = CheckoutRules.text(cmd.reason(), 500);
        var order = orders.findByOrderNoForUpdate(orderNo);
        if (order == null) throw new IllegalArgumentException("订单不存在");
        var previous = fulfillment.findRefundRequest(orderNo, key);
        if (previous != null) {
            if (!reason.equals(previous.reason())) throw new IllegalStateException("退款请求编号已用于不同内容");
            return previous;
        }
        if (!"PAID".equals(order.status())) throw new IllegalStateException("当前只支持发货前已支付订单的整单退款申请");
        if (fulfillment.activeRefund(orderNo)) throw new IllegalStateException("已有待处理退款申请");
        var successful =
                payments.findByOrderNo(orderNo).stream()
                        .filter(p -> "SUCCESS".equals(p.status()))
                        .toList();
        if (successful.size() != 1
                || order.totalAmount().compareTo(successful.get(0).amount()) != 0)
            throw new IllegalStateException("成功支付记录与订单金额不一致，请先对账");
        String refundNo =
                "RF"
                        + UUID.randomUUID()
                                .toString()
                                .replace("-", "")
                                .substring(0, 24)
                                .toUpperCase(Locale.ROOT);
        var request =
                new RefundRequest(
                        refundNo,
                        orderNo,
                        key,
                        order.memberId(),
                        successful.get(0).paymentNo(),
                        order.totalAmount(),
                        reason,
                        "REQUESTED",
                        null,
                        orders.databaseNow(),
                        null);
        fulfillment.saveRefundRequest(request);
        return fulfillment.refund(refundNo);
    }

    @Transactional(rollbackFor = Exception.class)
    public RefundRequest rejectRefund(RefundRejectCmd cmd) {
        if (cmd == null) throw new IllegalArgumentException("退款审核参数不能为空");
        String no = CheckoutRules.text(cmd.refundNo(), 40);
        String reason = CheckoutRules.text(cmd.reason(), 500);
        var initial = fulfillment.refund(no);
        if (initial == null) throw new IllegalArgumentException("退款申请不存在");
        if (orders.findByOrderNoForUpdate(initial.orderNo()) == null)
            throw new IllegalStateException("订单不存在");
        var request = fulfillment.lockRefund(no);
        if ("REJECTED".equals(request.status())) {
            if (!reason.equals(request.decisionReason()))
                throw new IllegalStateException("已有不同的审核记录");
            return request;
        }
        if (!"REQUESTED".equals(request.status())
                || !fulfillment.rejectRefund(no, reason, orders.databaseNow()))
            throw new IllegalStateException("退款申请状态冲突");
        return fulfillment.refund(no);
    }
}
