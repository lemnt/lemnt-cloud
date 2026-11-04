package com.lemnt.order.domain.order.gateway;

import com.lemnt.order.domain.order.model.OrderResourcePlan;

/** 查询订单自己保存的券/活动及SKU标识，绝不反查当前商品补历史。 */
public interface OrderResourceGateway {
    OrderResourcePlan plan(String orderNo);
}
