package com.lemnt.trade.application.payment.executor;

import com.lemnt.trade.application.order.service.OrderPersistenceService;
import com.lemnt.trade.application.payment.dto.MemberPaymentStatusDTO;
import com.lemnt.trade.application.payment.qry.MemberPaymentStatusQry;
import com.lemnt.trade.domain.order.model.OrderRules;
import com.lemnt.trade.domain.payment.gateway.TradePaymentGateway;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import java.util.Set;

/**
 * 会员支付状态查询执行器。
 */
@Component
@RequiredArgsConstructor
public class MemberPaymentStatusQueryExe {
    private final OrderPersistenceService orderPersistenceService;
    private final TradePaymentGateway tradePaymentGateway;

    /**
     * 执行会员支付状态查询。
     */
    @Transactional(readOnly = true)
    public MemberPaymentStatusDTO execute(MemberPaymentStatusQry query) {
        if (query == null) {
            throw new IllegalArgumentException("查询参数不能为空");
        }
        OrderRules.id(query.memberId());
        String no = OrderRules.text(query.orderNo(), 40);
        var order = orderPersistenceService.findByOrderNo(no);
        if (order == null || !query.memberId().equals(order.memberId())) {
            return null;
        }
        var records = tradePaymentGateway.findByOrderNo(no);
        long success = records.stream().filter(p -> "SUCCESS".equals(p.status())).count();
        boolean waiting = records.stream().anyMatch(p -> "WAITING".equals(p.status()));
        boolean paid =
                order.status() != null
                && Set.of("PAID", "SHIPPED", "COMPLETED", "REFUNDED").contains(order.status());
        boolean invalid =
                order.status() == null
                || !Set.of(
                "RESERVING",
                "PENDING_PAYMENT",
                "PAID",
                "SHIPPED",
                "COMPLETED",
                "REFUNDED",
        "CLOSED")
                .contains(order.status())
                || records.stream()
                .anyMatch(
                p ->
                !no.equals(p.orderNo())
                || p.amount() == null
                || p.amount().compareTo(order.totalAmount())
                != 0
                || p.status() == null
                || !Set.of("SUCCESS", "WAITING", "CLOSED")
                .contains(p.status()));
        String state;
        if (invalid
                || success > 1
                ||(success == 1 && !paid)
                ||(paid && success != 1)
                ||("CLOSED".equals(order.status()) && waiting)
                ||("RESERVING".equals(order.status()) && !records.isEmpty())) {
            state = "RECONCILE_REQUIRED";
        }
        else if (success == 1) {
            state = "SUCCESS";
        }
        else if ("CLOSED".equals(order.status())) {
            state = "CLOSED";
        }
        else if (waiting) {
            state = "WAITING";
        }
        else if (records.isEmpty()) {
            state = "NOT_CREATED";
        }
        else {
            state = "CLOSED";
        }
        return new MemberPaymentStatusDTO(no, order.status(), order.totalAmount(), state, false);
    }
}
