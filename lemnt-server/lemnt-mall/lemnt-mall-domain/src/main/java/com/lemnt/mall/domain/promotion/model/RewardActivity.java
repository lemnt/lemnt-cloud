package com.lemnt.mall.domain.promotion.model;

import java.time.LocalDateTime;
import java.util.List;

/** 满减活动，直接维护业务规则。 */
public record RewardActivity(
        Long id,
        String name,
        Integer status,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String remark,
        String conditionType,
        String productScope,
        List<Long> productScopeValues,
        List<RewardRule> rules
) implements java.io.Serializable {
}
