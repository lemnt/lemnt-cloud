package com.lemnt.mall.application.promotion.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** 后台秒杀活动查询结果，含商品简称与活动商品。 */
public record SeckillActivityDTO(
        Long id,
        Long spuId,
        String spuName,
        String picUrl,
        BigDecimal seckillPrice,
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
        Integer totalStock,
        List<SeckillProductDTO> products
) {}
