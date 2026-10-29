package com.lemnt.mall.adapter.app.product.converter;

import com.lemnt.mall.adapter.app.product.req.CommentCreateReq;
import com.lemnt.mall.application.product.cmd.CommentCreateCmd;

/** 会员评价请求转换，会员身份来自网关。 */
public final class CommentReqConverter {
    private CommentReqConverter() {
    }

    /** 不接受客户端指定会员和显示状态。 */
    public static CommentCreateCmd create(Long memberId, CommentCreateReq req) {
        return new CommentCreateCmd(memberId, req.orderNo(), req.skuId(), req.score(), req.content(), req.descriptionScores(), req.benefitScores(), req.anonymous() == null ? false : req.anonymous(),
                req.picUrls() == null ? java.util.List.of() : req.picUrls());
    }
    /** App只绑定公开分页合同，显隐固定在领域存储条件中。 */
    public static com.lemnt.mall.application.product.qry.CommentPublicPageQry page(
            com.lemnt.mall.adapter.app.product.req.CommentPageReq req) {
        return new com.lemnt.mall.application.product.qry.CommentPublicPageQry(req.spuId(),
                req.type() == null ? 0 : req.type(), req.pageNum() == null ? 1 : req.pageNum(),
                req.pageSize() == null ? 20 : req.pageSize());
    }
}
