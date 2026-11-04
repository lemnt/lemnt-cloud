package com.lemnt.product.domain.product.model;

/**
 * 商品公开评价分页，只接受SPU及评分分组。
 */
public record ProductPublicCommentFilter(Long spuId, int type, int pageNum, int pageSize) {
}
