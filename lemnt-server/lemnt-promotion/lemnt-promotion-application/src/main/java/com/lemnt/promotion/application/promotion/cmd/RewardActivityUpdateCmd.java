package com.lemnt.promotion.application.promotion.cmd;

import com.lemnt.promotion.domain.promotion.model.RewardRule;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 修改活动命令。
 */
public record RewardActivityUpdateCmd(
    Long id,
    String name,
    LocalDateTime startTime,
    LocalDateTime endTime,
    String remark,
    String conditionType,
    String productScope,
    List<Long> productScopeValues,
    List<RewardRule> rules) {
}
