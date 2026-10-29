package com.lemnt.mall.domain.product.model;

/** 商品收藏记录。 */
public record ProductFavorite(Long id, Long userId, Long spuId, java.time.LocalDateTime createTime) {}
