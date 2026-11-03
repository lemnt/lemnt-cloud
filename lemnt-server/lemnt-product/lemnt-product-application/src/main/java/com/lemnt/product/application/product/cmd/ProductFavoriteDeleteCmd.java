package com.lemnt.product.application.product.cmd;

/** remove用例参数，会员身份来自认证上下文。 */
public record ProductFavoriteDeleteCmd(Long userId, Long spuId) {}
