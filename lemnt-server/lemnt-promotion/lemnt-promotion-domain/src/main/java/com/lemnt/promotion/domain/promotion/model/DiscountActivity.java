package com.lemnt.promotion.domain.promotion.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 限时折扣活动。
 */
public record DiscountActivity(
    Long id,
    String name,
    Integer status,
    LocalDateTime startTime,
    LocalDateTime endTime,
    String remark)
    implements Serializable {
}
