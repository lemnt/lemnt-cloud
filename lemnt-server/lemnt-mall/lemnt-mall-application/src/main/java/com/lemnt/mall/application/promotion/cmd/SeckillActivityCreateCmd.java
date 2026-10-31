package com.lemnt.mall.application.promotion.cmd;

import java.time.LocalDateTime;
import java.util.List;

/** 新增秒杀活动命令。 */
public record SeckillActivityCreateCmd(
        Long spuId,
        String name,
        String remark,
        LocalDateTime startTime,
        LocalDateTime endTime,
        Integer sort,
        List<Long> configIds,
        Integer totalLimitCount,
        Integer singleLimitCount,
        List<SeckillProductCmd> products
) {}
