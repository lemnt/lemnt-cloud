package com.lemnt.mall.application.promotion.cmd;

import com.lemnt.mall.domain.promotion.model.RewardRule;

import java.time.LocalDateTime;
import java.util.List;

/** 创建活动命令。 */
public record RewardActivityCreateCmd(
        String name,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String remark,
        String conditionType,
        String productScope,
        List<Long> productScopeValues,
        List<RewardRule> rules
) {}
