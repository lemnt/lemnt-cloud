package com.lemnt.trade.application.order.executor;

import com.lemnt.trade.application.order.dto.OrderRemarkDTO;
import com.lemnt.trade.domain.order.gateway.TradeOrderGateway;
import com.lemnt.trade.domain.order.model.OrderRules;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 后台订单备注查询。 */
@Component
@RequiredArgsConstructor
public class OrderRemarkDetailQueryExe {
    private final TradeOrderGateway orders;

    /** 不存在的订单返回空；未填备注统一为空字符串。 */
    public OrderRemarkDTO execute(String orderNo) {
        String no = OrderRules.text(orderNo, 40);
        if (orders.findHeader(no) == null) {
            return null;
        }
        var remark = orders.remark(no);
        return new OrderRemarkDTO(remark == null ? "" : remark);
    }
}
