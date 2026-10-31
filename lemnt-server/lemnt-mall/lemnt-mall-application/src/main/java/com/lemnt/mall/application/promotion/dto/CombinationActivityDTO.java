package com.lemnt.mall.application.promotion.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** 拼团活动查询结果。 */
public record CombinationActivityDTO(
        Long id,
        String name,
        Long spuId,
        String spuName,
        String picUrl,
        BigDecimal combinationPrice,
        Integer totalLimitCount,
        Integer singleLimitCount,
        LocalDateTime startTime,
        LocalDateTime endTime,
        Integer userSize,
        Boolean virtualGroup,
        Integer status,
        Integer limitDuration,
        List<CombinationProductDTO> products
) {}
