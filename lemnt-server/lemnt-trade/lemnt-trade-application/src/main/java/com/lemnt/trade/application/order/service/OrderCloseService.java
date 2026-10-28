package com.lemnt.trade.application.order.service;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.trade.domain.order.model.TradeOperator;


import com.lemnt.trade.application.order.service.OrderPersistenceService;
import com.lemnt.trade.domain.order.gateway.TradeOrderGateway;
import com.lemnt.trade.domain.order.model.TradeOrder;
import com.lemnt.trade.domain.payment.gateway.PaymentChannelCloseGateway;
import com.lemnt.trade.domain.payment.gateway.TradePaymentGateway;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import java.util.Set;

/**
 * 交易订单关闭命令执行器。
 */
@Component
@RequiredArgsConstructor
public class OrderCloseService {
    private final OrderPersistenceService orderPersistenceService;
    private final TradeOrderGateway tradeOrderGateway;
    private final TradePaymentGateway payments;
    private final PaymentChannelCloseGateway channels;
    private final OrderLogService logs;

    /**
     * 在本地事务关闭未支付订单，提交后由调用方发送库存与券统一补偿。
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean execute(String orderNo) {
        return execute(orderNo, TradeOperator.system());
    }

    /** 会员取消核对归属，后台定时关单显式使用系统操作者。 */
    @Transactional(rollbackFor = Exception.class)
    public boolean execute(String orderNo, TradeOperator operator) {
        if (orderNo == null || orderNo.isBlank()) {
            throw new IllegalArgumentException("订单号不能为空");
        }
        TradeOrder order = orderPersistenceService.findByOrderNoForUpdate(orderNo);
        if (order == null) {
            throw new IllegalArgumentException("订单不存在");
        }
        if (operator.userType() == 1 && !operator.userId().equals(order.memberId())) {
            throw new ServiceException("订单不存在", 400);
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
        logs.record(order, order.status(), "CLOSED", operator.userType() == 1 ? 40 : 41, operator,
                operator.userType() == 1 ? "用户取消订单" : "系统取消未支付订单");
        return true;
    }
}
