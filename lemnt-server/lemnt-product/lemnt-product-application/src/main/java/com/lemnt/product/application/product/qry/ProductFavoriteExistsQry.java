package com.lemnt.product.application.product.qry;

/** exists用例参数，会员身份来自认证上下文。 */
public record ProductFavoriteExistsQry(Long userId, Long spuId) {}
