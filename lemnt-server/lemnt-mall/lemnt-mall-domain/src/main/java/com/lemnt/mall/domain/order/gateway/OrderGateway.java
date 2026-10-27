package com.lemnt.mall.domain.order.gateway;

import com.lemnt.mall.domain.order.model.OrderSnapshot;

/** 交易服务订单契约，商城不访问订单数据库。 */
public interface OrderGateway {
    /** 以稳定订单号幂等创建订单及预占库存事件。 */
    OrderSnapshot save(OrderSnapshot order);

    /** 查询交易服务中的订单快照。 */
    OrderSnapshot findByOrderNo(String orderNo);
}
