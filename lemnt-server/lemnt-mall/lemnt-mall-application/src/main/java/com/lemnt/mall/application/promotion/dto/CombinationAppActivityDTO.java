package com.lemnt.mall.application.promotion.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** App 拼团活动摘要。 */
public record CombinationAppActivityDTO(
        Long id,
        String name,
        Long spuId,
        String spuName,
        String picUrl,
        BigDecimal marketPrice,
        BigDecimal combinationPrice,
        Integer status,
        LocalDateTime startTime,
        LocalDateTime endTime,
        Integer userSize
) {}
