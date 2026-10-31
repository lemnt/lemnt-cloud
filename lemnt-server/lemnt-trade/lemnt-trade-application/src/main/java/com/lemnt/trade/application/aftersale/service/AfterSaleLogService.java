package com.lemnt.trade.application.aftersale.service;

import com.lemnt.trade.domain.aftersale.gateway.AfterSaleLogGateway;
import com.lemnt.trade.domain.aftersale.model.AfterSale;
import com.lemnt.trade.domain.aftersale.model.AfterSaleLog;
import com.lemnt.trade.domain.order.gateway.TradeOrderGateway;
import com.lemnt.trade.domain.order.model.TradeOperator;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/** 售后日志与申请、审批、退货、退款在同一业务事务内提交。 */
@Component
@RequiredArgsConstructor
public class AfterSaleLogService {
    private final AfterSaleLogGateway logs;
    private final TradeOrderGateway orders;

    /** 使用真实售后数值主键，不以业务单号冒充关联。 */
    @Transactional(propagation = Propagation.MANDATORY)
    public void record(AfterSale value, String before, String after, int type, TradeOperator operator, String content) {
        if (value.id() == null) { throw new IllegalStateException("售后主键未写入"); }
        logs.append(new AfterSaleLog(null, operator.userId(), operator.userType(), value.id(), status(before), status(after),
                type, content, orders.databaseNow()));
    }

    /** 当前支付退款处理中仍对应芋道等待退款状态40。 */
    private Integer status(String value) {
        if (value == null) { return null; }
        return switch (value) {
            case "REQUESTED" -> 10;
            case "AWAITING_RETURN" -> 20;
            case "RETURNED" -> 30;
            case "WAIT_REFUND", "REFUNDING" -> 40;
            case "REFUNDED" -> 50;
            case "CANCELLED" -> 61;
            case "REJECTED" -> 62;
            case "RETURN_REJECTED" -> 63;
            default -> throw new IllegalArgumentException("售后日志状态不合法");
        };
    }
}
