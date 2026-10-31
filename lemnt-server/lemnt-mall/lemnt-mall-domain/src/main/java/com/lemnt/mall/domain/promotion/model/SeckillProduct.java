package com.lemnt.mall.domain.promotion.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** 秒杀活动商品，保存活动状态与时间快照供 App 展示。 */
public record SeckillProduct(
        Long id,
        Long activityId,
        List<Long> configIds,
        Long spuId,
        Long skuId,
        BigDecimal seckillPrice,
        Integer stock,
        Integer activityStatus,
        LocalDateTime activityStartTime,
        LocalDateTime activityEndTime
) implements java.io.Serializable {
    public SeckillProduct {
        configIds = configIds == null ? List.of() : List.copyOf(configIds);
    }
}
