package com.lemnt.mall.domain.promotion.model;

import java.math.BigDecimal;

/** 砍价助力记录。 */
public record BargainHelp(Long id, Long activityId, Long recordId, Long userId, BigDecimal reducePrice)
        implements java.io.Serializable {
}
