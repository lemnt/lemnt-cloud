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
        int pageSize
) implements Serializable {
}
