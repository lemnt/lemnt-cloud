package com.lemnt.mall.domain.promotion.gateway;

import com.lemnt.mall.domain.promotion.model.PointProduct;

import java.util.List;

/** 积分商城活动商品存储契约。 */
public interface PointProductGateway {
    List<PointProduct> byActivityId(Long activityId);

    List<PointProduct> byActivityIds(List<Long> activityIds);

    PointProduct byActivityAndSku(Long activityId, Long skuId);

    void replace(Long activityId, List<PointProduct> products);

    void deleteByActivity(Long activityId);

    boolean decreaseStock(Long activityId, Long skuId, int count);

    boolean increaseStock(Long activityId, Long skuId, int count);

    /** 关闭活动时同步商品快照状态。 */
    void updateStatus(Long activityId, Integer status);
}
