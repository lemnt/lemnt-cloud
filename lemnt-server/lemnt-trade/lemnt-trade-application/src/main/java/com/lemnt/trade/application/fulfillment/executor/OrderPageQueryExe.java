package com.lemnt.trade.application.fulfillment.executor;

import com.lemnt.trade.application.fulfillment.cmd.*;
import com.lemnt.trade.application.fulfillment.dto.*;
import com.lemnt.trade.application.fulfillment.converter.FulfillmentDTOConverter;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

import com.lemnt.trade.application.fulfillment.qry.OrderPageQry;
import com.lemnt.trade.domain.fulfillment.gateway.FulfillmentGateway;
import com.lemnt.trade.domain.fulfillment.model.FulfillmentFilter;
import com.lemnt.trade.domain.checkout.model.CheckoutRules;

@Component
@RequiredArgsConstructor
public class OrderPageQueryExe {
    private final FulfillmentGateway fulfillment;

    public OrderPageDTO execute(OrderPageQry query) {
        if (query == null
                || query.pageNum() < 1
                || query.pageNum() > 1000000
                || query.pageSize() < 1
                || query.pageSize() > 100) throw new IllegalArgumentException("分页参数不合法");
        String no =
                query.orderNo() == null || query.orderNo().isBlank()
                        ? null
                        : CheckoutRules.text(query.orderNo(), 40);
        String status = query.status() == null || query.status().isBlank() ? null : query.status();
        if (status != null
                && !java.util.Set.of(
                                "RESERVING",
                                "PENDING_PAYMENT",
                                "PAID",
                                "SHIPPED",
                                "COMPLETED",
                                "CLOSED")
                        .contains(status)) throw new IllegalArgumentException("状态参数不合法");
        var page =
                fulfillment.orders(
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
