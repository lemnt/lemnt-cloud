package com.lemnt.mall.application.product.cmd;

/** 当前认证会员发起BrowseHistory，身份来自适配层。 */
public record ProductBrowseHistoryCreateCmd(Long userId, Long spuId) {}
