package com.lemnt.mall.application.product.cmd;

/** remove用例参数，会员身份来自认证上下文。 */
public record ProductFavoriteDeleteCmd(Long userId, Long spuId) {}
