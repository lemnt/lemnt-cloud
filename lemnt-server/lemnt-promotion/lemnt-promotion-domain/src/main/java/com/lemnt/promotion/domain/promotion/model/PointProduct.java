package com.lemnt.promotion.domain.promotion.model;

import java.io.Serializable;
import java.math.BigDecimal;

/** 积分商城活动商品：兑换积分、补差金额、限兑数量和库存。 */
public record PointProduct(
        Long id,
        Long activityId,
        Long spuId,
        Long skuId,
        Integer exchangeCount,
        Integer point,
        BigDecimal price,
        Integer stock,
        Integer activityStatus)
        implements Serializable {}
