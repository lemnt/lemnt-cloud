package com.lemnt.trade.application.order.service;

import com.lemnt.trade.domain.order.gateway.OrderLogGateway;
import com.lemnt.trade.domain.order.gateway.TradeOrderGateway;
import com.lemnt.trade.domain.order.model.OrderLog;
import com.lemnt.trade.domain.order.model.TradeOperator;
import com.lemnt.trade.domain.order.model.TradeOrder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** 真实订单操作与状态在调用方同一事务内落库。 */
@Component
@RequiredArgsConstructor
public class OrderLogService {
    private final OrderLogGateway logs;
    private final TradeOrderGateway orders;

    /** 必须在业务事务内调用，日志失败时一并回滚订单状态。 */
    @Transactional(propagation = Propagation.MANDATORY)
    public void record(
            TradeOrder order,
            String before,
            String after,
            int operateType,
            TradeOperator operator,
            String content) {
        logs.append(new OrderLog(null, operator.userId(), operator.userType(), order.id(), status(before), status(after),
                operateType, content, orders.databaseNow()));
    }

    /** 内部预占中和待支付都对应源项目待支付状态。 */
    private Integer status(String value) {
        if (value == null) { return null; }
        return switch (value) {
            case "RESERVING", "PENDING_PAYMENT" -> 0;
            case "PAID" -> 10;
            case "SHIPPED" -> 20;
            case "COMPLETED" -> 30;
            case "CLOSED", "REFUNDED" -> 40;
            default -> throw new IllegalArgumentException("订单日志状态不合法");
        };
    }
}
