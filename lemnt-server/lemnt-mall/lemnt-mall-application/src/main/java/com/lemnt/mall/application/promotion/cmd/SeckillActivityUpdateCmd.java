package com.lemnt.mall.application.promotion.cmd;

import java.time.LocalDateTime;
import java.util.List;

/** 修改秒杀活动命令。 */
public record SeckillActivityUpdateCmd(
        Long id,
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
