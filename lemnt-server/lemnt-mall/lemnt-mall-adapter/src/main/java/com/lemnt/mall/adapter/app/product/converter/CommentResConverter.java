package com.lemnt.mall.adapter.app.product.converter;

import com.lemnt.mall.adapter.app.product.res.CommentCreateRes;
import com.lemnt.mall.application.product.dto.ProductCommentDTO;

/** 会员评价创建响应转换。 */
public final class CommentResConverter {
    private CommentResConverter() {
    }

    /** 返回稳定的评价编号。 */
    public static CommentCreateRes created(ProductCommentDTO value) {
        return new CommentCreateRes(value.id());
    }
}
