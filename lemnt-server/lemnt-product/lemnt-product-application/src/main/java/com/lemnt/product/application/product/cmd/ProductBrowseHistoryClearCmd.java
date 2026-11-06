package com.lemnt.product.application.product.cmd;

/**
 * clear用例参数，会员身份来自认证上下文。
 */
public record ProductBrowseHistoryClearCmd(Long userId) {
}
