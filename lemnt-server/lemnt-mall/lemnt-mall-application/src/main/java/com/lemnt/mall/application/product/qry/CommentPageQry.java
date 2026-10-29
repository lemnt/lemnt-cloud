package com.lemnt.mall.application.product.qry;

import java.io.Serializable;

/** 评价分页查询。 */
public record CommentPageQry(
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
    public CommentPageQry(Long spuId, Long memberId, Integer score, Boolean visible, Boolean replied, int pageNum, int pageSize) {
        this(spuId, memberId, score, visible, replied, pageNum, pageSize, null, null, null, null, null);
    }
}
