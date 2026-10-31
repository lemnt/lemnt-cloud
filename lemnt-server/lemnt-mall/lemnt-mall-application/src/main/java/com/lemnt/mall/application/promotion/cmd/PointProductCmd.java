package com.lemnt.mall.application.promotion.cmd;

import java.math.BigDecimal;

/** 积分商城活动商品行。 */
public record PointProductCmd(Long skuId, Integer exchangeCount, Integer point, BigDecimal price, Integer stock) {}
