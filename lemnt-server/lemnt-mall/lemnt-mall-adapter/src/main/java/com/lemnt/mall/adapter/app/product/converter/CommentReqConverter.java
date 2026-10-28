package com.lemnt.mall.adapter.app.product.converter;

import com.lemnt.mall.adapter.app.product.req.CommentCreateReq;
import com.lemnt.mall.application.product.cmd.CommentCreateCmd;

/** 会员评价请求转换，会员身份来自网关。 */
public final class CommentReqConverter {
    private CommentReqConverter() {
    }

    /** 不接受客户端指定会员和显示状态。 */
    public static CommentCreateCmd create(Long memberId, CommentCreateReq req) {
        return new CommentCreateCmd(memberId, req.orderNo(), req.skuId(), req.score(), req.content());
    }
}
