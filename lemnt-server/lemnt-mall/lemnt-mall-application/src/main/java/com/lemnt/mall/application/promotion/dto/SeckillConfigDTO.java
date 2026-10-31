package com.lemnt.mall.application.promotion.dto;

import java.util.List;

/** 秒杀时段查询结果。 */
public record SeckillConfigDTO(
        Long id,
        String name,
        String startTime,
        String endTime,
        List<String> sliderPicUrls,
        Integer status,
        Integer activityCount
) {}
