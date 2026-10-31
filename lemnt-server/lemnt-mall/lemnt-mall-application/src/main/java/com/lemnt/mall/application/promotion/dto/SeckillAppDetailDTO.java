package com.lemnt.mall.application.promotion.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** App 秒杀活动详情，时间为当天时段换算结果。 */
public record SeckillAppDetailDTO(
        Long id,
        String name,
        Integer status,
        LocalDateTime startTime,
        LocalDateTime endTime,
        Long spuId,
        Integer totalLimitCount,
        Integer singleLimitCount,
        Integer stock,
        Integer totalStock,
        List<Product> products
) {
    /** 详情中的秒杀商品。 */
    public record Product(Long skuId, BigDecimal seckillPrice, Integer stock) {}
}
