package com.lemnt.mall.domain.promotion.gateway;

import com.lemnt.mall.domain.promotion.model.DiscountProduct;

import java.util.List;

/** 折扣活动商品表访问。 */
public interface DiscountProductGateway {
    List<DiscountProduct> byActivities(List<Long> activityIds);
    void replace(Long activityId, List<DiscountProduct> products);
    void deleteByActivity(Long activityId);
}
