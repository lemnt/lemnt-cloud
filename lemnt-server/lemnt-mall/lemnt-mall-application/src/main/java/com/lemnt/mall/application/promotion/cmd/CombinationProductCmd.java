package com.lemnt.mall.application.promotion.cmd;

import java.math.BigDecimal;

/** 拼团活动商品行。 */
public record CombinationProductCmd(Long skuId, BigDecimal combinationPrice) {}
