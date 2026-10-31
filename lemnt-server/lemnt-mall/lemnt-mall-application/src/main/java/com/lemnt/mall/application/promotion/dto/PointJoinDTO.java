package com.lemnt.mall.application.promotion.dto;

import java.math.BigDecimal;

/** 积分兑换前校验结果，供订单计价使用。 */
public record PointJoinDTO(Long activityId, Integer point, BigDecimal price, Integer exchangeCount) {}
