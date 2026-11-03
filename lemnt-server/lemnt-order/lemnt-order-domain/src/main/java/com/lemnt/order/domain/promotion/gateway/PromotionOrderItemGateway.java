package com.lemnt.order.domain.promotion.gateway;

import com.lemnt.order.domain.promotion.model.PromotionOrderItem;

import java.util.List;

/** 活动订单商品行存储契约。 */
public interface PromotionOrderItemGateway {
    List<PromotionOrderItem> byOrderNo(String orderNo);

    void saveAll(String orderNo, List<PromotionOrderItem> items);

    /** 条件累加商品行已退款数量。 */
    boolean increaseRefunded(String orderNo, Long skuId, Integer expected, Integer quantity);
}
