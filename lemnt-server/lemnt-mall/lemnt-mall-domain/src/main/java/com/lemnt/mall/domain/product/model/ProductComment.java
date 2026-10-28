package com.lemnt.mall.domain.product.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/** 真实订单商品评价。 */
public record ProductComment(
        Long id,
        Long spuId,
        Long skuId,
        String orderNo,
        Long memberId,
        Integer score,
        String content,
        boolean visible,
        String replyContent,
        Long replyUserId,
        LocalDateTime replyTime,
        LocalDateTime createTime
) implements Serializable {
}
