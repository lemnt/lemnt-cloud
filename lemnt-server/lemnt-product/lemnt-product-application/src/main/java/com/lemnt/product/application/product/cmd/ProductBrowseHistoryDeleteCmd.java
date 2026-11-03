package com.lemnt.product.application.product.cmd;

/** hide用例参数，会员身份来自认证上下文。 */
public record ProductBrowseHistoryDeleteCmd(Long userId, java.util.List<Long> spuIds) {}
