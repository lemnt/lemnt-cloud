package com.lemnt.mall.application.product.executor;

import com.lemnt.mall.application.product.cmd.CommentVisibleCmd;
import com.lemnt.mall.domain.product.gateway.ProductCommentGateway;
import com.lemnt.common.core.exception.ServiceException;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 商品评价显示管理。 */
@Component
@RequiredArgsConstructor
public class CommentVisibleCmdExe {
    private final ProductCommentGateway productCommentGateway;

    /** 显示状态不影响评价正文和评分。 */
    public boolean execute(CommentVisibleCmd cmd) {
        if (cmd == null || cmd.id() == null || cmd.id() <= 0) {
            throw new ServiceException("评价编号不合法", 400);
        }
        if (!productCommentGateway.visible(cmd.id(), cmd.visible())) {
            throw new ServiceException("评价不存在", 404);
        }
        return true;
    }
}
