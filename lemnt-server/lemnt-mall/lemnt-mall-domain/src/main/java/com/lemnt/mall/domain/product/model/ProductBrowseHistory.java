package com.lemnt.mall.domain.product.model;

/** 浏览足迹记录。 */
public record ProductBrowseHistory(Long id, Long userId, Long spuId, boolean userDeleted, java.time.LocalDateTime createTime) {}
