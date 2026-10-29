package com.lemnt.mall.application.product.qry;

/** Favorite分页，web可筛选会员，app固定本人。 */
public record ProductFavoritePageQry(Long userId, Long spuId, int pageNum, int pageSize) {}
