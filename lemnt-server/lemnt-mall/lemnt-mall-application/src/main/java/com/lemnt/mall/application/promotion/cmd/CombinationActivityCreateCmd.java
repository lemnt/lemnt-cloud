package com.lemnt.mall.application.promotion.cmd;

import java.time.LocalDateTime;
import java.util.List;

/** 新增拼团活动命令。 */
public record CombinationActivityCreateCmd(
        String name,
        Long spuId,
        Integer totalLimitCount,
        Integer singleLimitCount,
        LocalDateTime startTime,
        LocalDateTime endTime,
        Integer userSize,
        Boolean virtualGroup,
        Integer limitDuration,
        List<CombinationProductCmd> products
) {}
