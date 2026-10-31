package com.lemnt.mall.application.product.cmd;

/** 当前认证会员发起Favorite，身份来自适配层。 */
public record ProductFavoriteCreateCmd(Long userId, Long spuId) {}
