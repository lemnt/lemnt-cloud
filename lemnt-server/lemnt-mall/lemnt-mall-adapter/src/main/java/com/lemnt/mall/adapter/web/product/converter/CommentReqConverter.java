package com.lemnt.mall.adapter.web.product.converter;

import com.lemnt.mall.adapter.web.product.req.CommentPageReq;
import com.lemnt.mall.adapter.web.product.req.CommentReplyReq;
import com.lemnt.mall.adapter.web.product.req.CommentVisibleReq;
import com.lemnt.mall.application.product.cmd.CommentReplyCmd;
import com.lemnt.mall.application.product.cmd.CommentVisibleCmd;
import com.lemnt.mall.application.product.qry.CommentPageQry;

/** 后台评价请求转换，操作者来自网关上下文。 */
public final class CommentReqConverter {
    private CommentReqConverter() {
    }

    /** 转换回复命令。 */
    public static CommentReplyCmd reply(Long id, Long operatorId, CommentReplyReq req) {
        return new CommentReplyCmd(id, operatorId, req.content());
    }

    /** 转换显示设置。 */
    public static CommentVisibleCmd visible(Long id, CommentVisibleReq req) {
        return new CommentVisibleCmd(id, req.visible());
    }

    /** 转换分页。 */
    public static CommentPageQry page(CommentPageReq req) {
        return new CommentPageQry(req.spuId(), req.memberId(), req.score(), req.visible(), req.replied(),
            req.pageNum() == null ? 1 : req.pageNum(), req.pageSize() == null ? 20 : req.pageSize());
    }
}
