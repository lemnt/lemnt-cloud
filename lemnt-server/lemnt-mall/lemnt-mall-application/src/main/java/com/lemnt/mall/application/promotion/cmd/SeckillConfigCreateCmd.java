package com.lemnt.mall.application.promotion.cmd;

import java.util.List;

/** 新增秒杀时段命令。 */
public record SeckillConfigCreateCmd(
        String name,
        String startTime,
        String endTime,
        List<String> sliderPicUrls,
        Integer status
) {}
