package com.lemnt.mall.domain.promotion.gateway;

import com.lemnt.mall.domain.promotion.model.CombinationProduct;

import java.util.List;

/** 拼团活动商品存储契约。 */
public interface CombinationProductGateway {
    List<CombinationProduct> byActivityId(Long activityId);

    List<CombinationProduct> byActivityIds(List<Long> activityIds);

    CombinationProduct byActivityAndSku(Long activityId, Long skuId);

    void replace(Long activityId, List<CombinationProduct> products);

    void deleteByActivity(Long activityId);
}
