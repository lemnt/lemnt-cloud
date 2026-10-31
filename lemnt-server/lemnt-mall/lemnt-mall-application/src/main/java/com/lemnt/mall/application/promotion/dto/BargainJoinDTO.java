package com.lemnt.mall.application.promotion.dto;

import java.math.BigDecimal;

/** 砍价成功后下单校验结果。 */
public record BargainJoinDTO(Long activityId, String name, BigDecimal bargainPrice) {}
