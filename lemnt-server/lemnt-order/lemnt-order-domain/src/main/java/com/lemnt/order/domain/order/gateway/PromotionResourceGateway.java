package com.lemnt.order.domain.order.gateway;

import com.lemnt.order.domain.order.model.OrderResourcePlan;

/**
 * 营销冻结与结果探测，订单只传保存的原资源标识。
 */
public interface PromotionResourceGateway {
    void lock(OrderResourcePlan plan);

    Long paidRecord(String orderNo);
}
