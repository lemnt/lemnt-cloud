package com.lemnt.mall.application.promotion.cmd;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 新增砍价活动命令。 */
public record BargainActivityCreateCmd(
        String name,
        LocalDateTime startTime,
        LocalDateTime endTime,
        Long spuId,
        Long skuId,
        BigDecimal bargainFirstPrice,
        BigDecimal bargainMinPrice,
        Integer stock,
        Integer helpMaxCount,
        Integer bargainCount,
        Integer totalLimitCount,
        BigDecimal randomMinPrice,
        BigDecimal randomMaxPrice
) {}
