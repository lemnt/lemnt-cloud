package com.lemnt.product.domain.product.model;

import java.time.LocalDateTime;

/**
 * 浏览足迹记录。
 */
public record ProductBrowseHistory(
    Long id, Long userId, Long spuId, boolean userDeleted, LocalDateTime createTime) {
}
