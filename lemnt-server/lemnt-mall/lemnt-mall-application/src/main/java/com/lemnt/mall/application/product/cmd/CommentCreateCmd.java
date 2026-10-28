package com.lemnt.mall.application.product.cmd;

import java.io.Serializable;

/** 会员评价创建。 */
public record CommentCreateCmd(
        Long memberId,
        String orderNo,
        Long skuId,
        Integer score,
        String content
) implements Serializable {
}
