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
        int pageSize
) implements Serializable {
}
