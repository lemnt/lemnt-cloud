package com.lemnt.mall.domain.product.model;

import java.time.LocalDateTime;

/** 商品收藏记录。 */
public record ProductFavorite(Long id, Long userId, Long spuId, LocalDateTime createTime) {}
