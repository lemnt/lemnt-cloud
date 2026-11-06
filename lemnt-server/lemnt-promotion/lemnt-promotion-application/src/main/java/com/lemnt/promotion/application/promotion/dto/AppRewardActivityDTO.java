package com.lemnt.promotion.application.promotion.dto;

import com.lemnt.promotion.domain.promotion.model.RewardRule;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 小程序满减送活动详情。
 */
public record AppRewardActivityDTO(
    Long id,
    String name,
    LocalDateTime startTime,
    LocalDateTime endTime,
    String conditionType,
    String productScope,
    List<RewardRule> rules)
    implements Serializable {
    public AppRewardActivityDTO {
        rules = rules == null ? List.of() : List.copyOf(rules);
    }
}
