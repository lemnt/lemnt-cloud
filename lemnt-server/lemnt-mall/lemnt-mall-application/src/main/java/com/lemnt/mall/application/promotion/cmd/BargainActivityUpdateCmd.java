package com.lemnt.mall.application.promotion.cmd;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 修改砍价活动命令。 */
public record BargainActivityUpdateCmd(
        Long id,
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
