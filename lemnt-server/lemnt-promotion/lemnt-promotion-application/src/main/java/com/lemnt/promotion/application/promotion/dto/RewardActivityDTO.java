package com.lemnt.promotion.application.promotion.dto;

import com.lemnt.promotion.domain.promotion.model.RewardRule;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 活动应用查询结果。
 */
public record RewardActivityDTO(
    Long id,
    String name,
    Integer status,
    LocalDateTime startTime,
    LocalDateTime endTime,
    String remark,
    String conditionType,
    String productScope,
    List<Long> productScopeValues,
    List<RewardRule> rules) {
}
