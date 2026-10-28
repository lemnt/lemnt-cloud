package com.lemnt.mall.application.product.executor;

import com.lemnt.mall.application.product.cmd.CommentVisibleCmd;
import com.lemnt.mall.domain.product.gateway.ProductCommentGateway;
import com.lemnt.mall.domain.product.model.ProductCatalogValidation;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 商品评价显示管理。 */
@Component
@RequiredArgsConstructor
public class CommentVisibleCmdExe {
    private final ProductCommentGateway productCommentGateway;

    /** 显示状态不影响评价正文和评分。 */
    public boolean execute(CommentVisibleCmd cmd) {
        if (cmd == null) {
            throw new IllegalArgumentException("显示设置不能为空");
        }
        ProductCatalogValidation.id(cmd.id());
        return productCommentGateway.visible(cmd.id(), cmd.visible());
    }
}
