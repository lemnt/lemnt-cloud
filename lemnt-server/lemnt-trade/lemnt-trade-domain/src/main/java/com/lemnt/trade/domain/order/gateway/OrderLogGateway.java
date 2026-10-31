package com.lemnt.trade.domain.order.gateway;

import com.lemnt.trade.domain.order.model.OrderLog;
import java.util.List;

/** 订单日志表存储契约，不更新订单主体。 */
public interface OrderLogGateway {
    /** 与订单状态变化同事务追加真实操作。 */
    void append(OrderLog value);
    /** 按发生顺序读取订单日志。 */
    List<OrderLog> byOrderId(Long orderId);
}
