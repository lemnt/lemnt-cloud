package com.lemnt.promotion.domain.order.gateway;

import com.lemnt.promotion.domain.order.model.PromotionOrderResources;

/**
 * 不访问订单库，只查询原冻结资源。
 */
public interface PromotionOrderGateway {
    PromotionOrderResources resources(String orderNo);
}
