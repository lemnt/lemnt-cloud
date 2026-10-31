package com.lemnt.trade.application.order.executor;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.trade.application.order.dto.OrderLogDTO;
import com.lemnt.trade.domain.order.gateway.OrderLogGateway;
import com.lemnt.trade.domain.order.gateway.TradeOrderGateway;
import com.lemnt.trade.domain.order.model.OrderRules;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.List;

/** 后台查询订单日志，不伪造迁移前历史。 */
@Component
@RequiredArgsConstructor
public class OrderLogListQueryExe {
    private final TradeOrderGateway orders;
    private final OrderLogGateway logs;

    /** 先确认订单主体，按日志编号顺序返回已提交记录。 */
    public List<OrderLogDTO> execute(String orderNo) {
        var order = orders.findHeader(OrderRules.text(orderNo, 40));
        if (order == null) { throw new ServiceException("订单不存在", 400); }
        return logs.byOrderId(order.id()).stream().map(value -> new OrderLogDTO(value.id(), value.userId(),
                value.userType(), value.orderId(), value.beforeStatus(), value.afterStatus(), value.operateType(),
                value.content(), value.createTime())).toList();
    }
}
