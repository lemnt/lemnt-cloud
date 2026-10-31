package com.lemnt.mall.application.promotion.dto;

import java.util.List;

/** 拼团概要：参团用户数与最近头像。 */
public record CombinationSummaryDTO(long userCount, List<String> avatars) {}
