package com.lemnt.order.application.event.service;

import com.lemnt.common.client.event.CommerceEventEnvelope;
import com.lemnt.common.client.event.CommerceEventTopics;
import com.lemnt.order.application.order.service.OrderLogService;
import com.lemnt.order.application.order.service.OrderPersistenceService;
import com.lemnt.order.application.promotion.service.ActivityOrderLifecycleService;
import com.lemnt.order.domain.event.gateway.InboundEventGateway;
import com.lemnt.order.domain.event.gateway.MessageInboxGateway;
import com.lemnt.order.domain.event.gateway.OrderEventGateway;
import com.lemnt.order.domain.order.gateway.TradeOrderGateway;
import com.lemnt.order.domain.order.model.TradeOperator;
import com.lemnt.order.domain.order.model.TradeOrder;
import com.lemnt.order.domain.payment.gateway.TradePaymentGateway;
import com.lemnt.order.domain.payment.model.PaymentRecord;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * 资金事实核对成功后才提交订单支付状态和权益事件，迟到资金留待对账，不能重新打开关单。
 */
@Component
@RequiredArgsConstructor
public class PaySuccessEventHandler {
    private final MessageInboxGateway inbox;
    private final OrderPersistenceService persistence;
    private final TradeOrderGateway orders;
    private final TradePaymentGateway payments;
    private final OrderLogService logs;
    private final ActivityOrderLifecycleService activities;
    private final OrderEventGateway events;
    private final InboundEventGateway inboundEvents;

    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public void execute(CommerceEventEnvelope envelope) {
        envelope.requireType(CommerceEventTopics.PAY_SUCCESS);
        var v = inboundEvents.paymentSucceededFacts(envelope.payload());
        if (v == null
                || !envelope.aggregateNo().equals(v.orderNo())
                || !envelope.businessKey().equals(v.paymentNo()))
            throw new IllegalArgumentException("支付事件标识不一致");
        var order = persistence.findByOrderNoForUpdate(v.orderNo());
        var paid = successfulPayments(v.orderNo());
        if (order == null
                || !order.memberId().equals(v.memberId())
                || v.amount() == null
                || v.amount().signum() <= 0
                || v.amount().compareTo(order.totalAmount()) != 0
                || paid.size() != 1
                || !paid.get(0).paymentNo().equals(v.paymentNo())
                || !Objects.equals(paid.get(0).channel(), v.channel())
                || !Objects.equals(paid.get(0).channelTradeNo(), v.channelTradeNo())
                || paid.get(0).amount().compareTo(v.amount()) != 0)
            throw new IllegalStateException("资金事实与原订单不一致，请先对账");
        confirm(order, paid.get(0));
    }

    /** 以原支付事实处理可重试通知，已确认的同一笔资金幂等返回。 */
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public void confirm(String orderNo, String paymentNo) {
        var order = persistence.findByOrderNoForUpdate(orderNo);
        var paid = successfulPayments(orderNo);
        if (order == null || paid.size() != 1 || !paid.get(0).paymentNo().equals(paymentNo)) {
            throw new IllegalStateException("原订单或唯一成功支付单不一致");
        }
        confirm(order, paid.get(0));
    }

    private void confirm(TradeOrder order, PaymentRecord payment) {
        if (Set.of("PAID", "SHIPPED", "COMPLETED", "REFUNDED").contains(order.status())) {
            var confirmed = orders.confirmedPaymentNo(order.orderNo());
            if (Objects.equals(confirmed, payment.paymentNo())
                    || (confirmed == null
                            && inbox.has(
                                    "order.pay_success.v1", "PAY_SUCCESS", payment.paymentNo()))) {
                return;
            }
            throw new IllegalStateException("订单已支付但缺少一致的资金确认事实，请对账");
        }
        if (!"PENDING_PAYMENT".equals(order.status()))
            throw new IllegalStateException("真实资金到达时订单状态不允许支付，请对账处理");
        markPaid(order, payment);
    }

    /** 消息丢失时只依据支付服务的成功事实补记订单，未支付订单保持原状态。 */
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public void recover(String orderNo) {
        var payments = successfulPayments(orderNo);
        if (payments.isEmpty()) {
            return;
        }
        if (payments.size() != 1) {
            throw new IllegalStateException("原订单有多笔成功资金，请先对账");
        }
        var order = persistence.findByOrderNoForUpdate(orderNo);
        if (order == null
                || !"PENDING_PAYMENT".equals(order.status())
                || orders.paymentPreparedAmount(orderNo) == null) {
            return;
        }
        markPaid(order, payments.get(0));
    }

    private List<PaymentRecord> successfulPayments(String orderNo) {
        return payments.findByOrderNo(orderNo).stream()
                .filter(payment -> "SUCCESS".equals(payment.status()))
                .toList();
    }

    private void markPaid(TradeOrder order, PaymentRecord payment) {
        if (!order.orderNo().equals(payment.orderNo())
                || payment.paymentNo() == null
                || payment.paymentNo().isBlank()
                || payment.channel() == null
                || payment.channel().isBlank()
                || payment.channelTradeNo() == null
                || payment.channelTradeNo().isBlank()
                || payment.amount() == null
                || payment.amount().signum() <= 0
                || payment.amount().compareTo(order.totalAmount()) != 0) {
            throw new IllegalStateException("资金事实与原订单不一致，请先对账");
        }
        if (!orders.confirmPayment(order.orderNo(), payment.paymentNo(), payment.amount()))
            throw new IllegalStateException("订单支付状态冲突");
        activities.paid(order.orderNo());
        events.paid(order);
        logs.record(order, "PENDING_PAYMENT", "PAID", 10, TradeOperator.system(), "原支付资金已核验");
    }
}
