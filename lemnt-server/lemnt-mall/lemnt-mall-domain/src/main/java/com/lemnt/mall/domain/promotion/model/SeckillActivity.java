package com.lemnt.mall.domain.promotion.model;

import java.time.LocalDateTime;
import java.util.List;

/** 秒杀活动，库存为活动商品库存合计。 */
public record SeckillActivity(
        Long id,
        Long spuId,
        String name,
        Integer status,
        String remark,
        LocalDateTime startTime,
        LocalDateTime endTime,
        Integer sort,
        List<Long> configIds,
        Integer totalLimitCount,
        Integer singleLimitCount,
        Integer stock,
        Integer totalStock
) implements java.io.Serializable {
    public SeckillActivity {
        configIds = configIds == null ? List.of() : List.copyOf(configIds);
    }
}
