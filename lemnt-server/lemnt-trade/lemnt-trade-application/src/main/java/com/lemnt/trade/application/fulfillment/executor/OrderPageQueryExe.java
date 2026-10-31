package com.lemnt.trade.application.fulfillment.executor;

import com.lemnt.trade.application.fulfillment.converter.FulfillmentDTOConverter;
import com.lemnt.trade.application.fulfillment.dto.OrderPageDTO;
import com.lemnt.trade.application.fulfillment.qry.OrderPageQry;
import com.lemnt.trade.domain.fulfillment.model.FulfillmentFilter;
import com.lemnt.trade.domain.order.gateway.TradeOrderGateway;
import com.lemnt.trade.domain.order.model.OrderRules;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.Set;

/**
 * 订单分页查询执行器。
 */
@Component
@RequiredArgsConstructor
public class OrderPageQueryExe {
    private final TradeOrderGateway tradeOrderGateway;

    /**
     * 执行订单分页查询。
     */
    public OrderPageDTO execute(OrderPageQry query) {
        if (query == null
                || query.pageNum() < 1
                || query.pageNum() > 1000000
                || query.pageSize() < 1
                || query.pageSize() > 100) {
            throw new IllegalArgumentException("分页参数不合法");
        }
        String no =
                query.orderNo() == null || query.orderNo().isBlank()
                ? null
                : OrderRules.text(query.orderNo(), 40);
        String status = query.status() == null || query.status().isBlank() ? null : query.status();
        if (status != null
                && !Set.of(
                "RESERVING",
                "PENDING_PAYMENT",
                "PAID",
                "SHIPPED",
                "COMPLETED",
                "REFUNDED",
        "CLOSED")
                .contains(status)) {
            throw new IllegalArgumentException("状态参数不合法");
        }
        var page =
                tradeOrderGateway.orders(
                new FulfillmentFilter(
                no,
                status,
                (query.pageNum() - 1) * query.pageSize(),
                query.pageSize()));
        return new OrderPageDTO(
                page.records().stream().map(FulfillmentDTOConverter::summary).toList(),
                page.total());
    }
}
