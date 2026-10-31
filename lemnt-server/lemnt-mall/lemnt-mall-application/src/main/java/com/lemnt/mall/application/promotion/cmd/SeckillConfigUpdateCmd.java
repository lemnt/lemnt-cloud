package com.lemnt.mall.application.promotion.cmd;

import java.util.List;

/** 修改秒杀时段命令。 */
public record SeckillConfigUpdateCmd(
        Long id,
        String name,
        String startTime,
        String endTime,
        List<String> sliderPicUrls,
        Integer status
) {}
