package com.lemnt.mall.application.product.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

/** 商品评价应用输出。 */
public record ProductCommentDTO(
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
