package com.lemnt.mall.application.promotion.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

/** 活动聚合条目，只包含聚合页跳转所需信息。 */
public record AppPromotionItemDTO(Long id, String name, LocalDateTime startTime, LocalDateTime endTime)
        implements Serializable {
}
