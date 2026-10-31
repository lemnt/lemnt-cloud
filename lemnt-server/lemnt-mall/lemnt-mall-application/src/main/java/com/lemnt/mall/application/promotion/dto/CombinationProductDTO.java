package com.lemnt.mall.application.promotion.dto;

import java.math.BigDecimal;

/** 拼团活动商品查询结果。 */
public record CombinationProductDTO(Long skuId, BigDecimal combinationPrice) {}
