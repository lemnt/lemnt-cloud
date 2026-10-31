package com.lemnt.mall.domain.promotion.gateway;

import com.lemnt.mall.domain.promotion.model.SeckillProduct;

import java.util.List;

/** 秒杀活动商品存储契约。 */
public interface SeckillProductGateway {
    List<SeckillProduct> byActivityId(Long activityId);

    List<SeckillProduct> byActivityIds(List<Long> activityIds);

    SeckillProduct byActivityAndSku(Long activityId, Long skuId);

    /** 整体替换活动商品，保留或重建明细。 */
    void replace(Long activityId, List<SeckillProduct> products);

    void deleteByActivity(Long activityId);

    /** 原子扣减活动商品库存，库存不足返回 false。 */
    boolean decreaseStock(Long activityId, Long skuId, int count);

    /** 恢复活动商品库存，活动或商品已删除时返回 false。 */
    boolean increaseStock(Long activityId, Long skuId, int count);
}
