package com.lemnt.mall.application.promotion.cmd;

import java.math.BigDecimal;

/** 秒杀活动商品行。 */
public record SeckillProductCmd(Long skuId, BigDecimal seckillPrice, Integer stock) {}
