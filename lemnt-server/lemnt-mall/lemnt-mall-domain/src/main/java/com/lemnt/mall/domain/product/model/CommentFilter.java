package com.lemnt.mall.domain.product.model;

import java.io.Serializable;

/** 评价查询条件。 */
public record CommentFilter(
        Long spuId,
        Long memberId,
        Integer score,
        Boolean visible,
        Boolean replied,
        int pageNum,
        int pageSize,
        String userNickname,
        Long orderId,
        String spuName,
        java.time.LocalDateTime createTimeStart,
        java.time.LocalDateTime createTimeEnd
) implements Serializable {
    /** 兼容现有筛选调用，新条件均为可选。 */
    public CommentFilter(Long spuId, Long memberId, Integer score, Boolean visible, Boolean replied, int pageNum, int pageSize) {
        this(spuId, memberId, score, visible, replied, pageNum, pageSize, null, null, null, null, null);
    }
}
