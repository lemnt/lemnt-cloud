package com.lemnt.mall.application.promotion.dto;

import java.math.BigDecimal;

/** 砍价助力查询结果。 */
public record BargainHelpDTO(Long id, Long activityId, Long recordId, Long userId, BigDecimal reducePrice) {}
