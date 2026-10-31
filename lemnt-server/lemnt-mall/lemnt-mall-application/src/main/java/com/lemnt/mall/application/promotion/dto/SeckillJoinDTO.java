package com.lemnt.mall.application.promotion.dto;

import java.math.BigDecimal;

/** 加入秒杀前校验结果，供订单计价使用。 */
public record SeckillJoinDTO(String name, Integer totalLimitCount, BigDecimal seckillPrice) {}
