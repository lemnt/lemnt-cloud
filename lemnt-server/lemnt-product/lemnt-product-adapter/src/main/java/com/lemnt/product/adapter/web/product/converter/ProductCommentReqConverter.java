package com.lemnt.product.adapter.web.product.converter;

import com.lemnt.product.adapter.web.product.req.ProductCommentPageReq;
import com.lemnt.product.adapter.web.product.req.ProductCommentReplyReq;
import com.lemnt.product.adapter.web.product.req.ProductCommentVisibleReq;
import com.lemnt.product.application.product.cmd.ProductCommentReplyCmd;
import com.lemnt.product.application.product.cmd.ProductCommentVisibleCmd;
import com.lemnt.product.application.product.qry.ProductCommentPageQry;

/**
 * 后台评价请求转换，操作者来自网关上下文。
 */
public final class ProductCommentReqConverter {
    private ProductCommentReqConverter() {
    }

    /**
     * 转换回复命令。
     */
    public static ProductCommentReplyCmd reply(Long id, Long operatorId, ProductCommentReplyReq req) {
        return new ProductCommentReplyCmd(id, operatorId, req.content());
    }

    /**
     * 转换显示设置。
     */
    public static ProductCommentVisibleCmd visible(Long id, ProductCommentVisibleReq req) {
        return new ProductCommentVisibleCmd(id, req.visible());
    }

    /**
     * 转换分页。
     */
    public static ProductCommentPageQry page(ProductCommentPageReq req) {
        return new ProductCommentPageQry(
            req.spuId(),
            req.memberId(),
            req.score(),
            req.visible(),
            req.replied(),
            req.pageNum() == null ? 1 : req.pageNum(),
            req.pageSize() == null ? 20 : req.pageSize(),
            req.userNickname(),
            req.orderId(),
            req.spuName(),
            req.createTimeStart(),
            req.createTimeEnd());
    }
}
