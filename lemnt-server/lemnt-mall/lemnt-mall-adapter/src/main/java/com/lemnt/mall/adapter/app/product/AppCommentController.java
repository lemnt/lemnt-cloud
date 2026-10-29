package com.lemnt.mall.adapter.app.product;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.utils.SecurityUtils;
import com.lemnt.mall.adapter.app.product.converter.CommentReqConverter;
import com.lemnt.mall.adapter.app.product.converter.CommentResConverter;
import com.lemnt.mall.adapter.app.product.req.CommentCreateReq;
import com.lemnt.mall.adapter.app.product.res.CommentCreateRes;
import com.lemnt.mall.application.product.executor.CommentCreateCmdExe;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import jakarta.validation.Valid;

/** 会员真实订单评价写入入口。 */
@Tag(name = "会员商品评价")
@RestController
@RequestMapping("/mall/app/comments")
@RequiredArgsConstructor
public class AppCommentController {
    private final CommentCreateCmdExe commentCreateCmdExe;
    private final com.lemnt.mall.application.product.executor.CommentPublicPageQueryExe commentPublicPageQueryExe;

    /** 公开查询当前商品可见评价，不接受其他会员或后台过滤条件。 */
    @org.springframework.web.bind.annotation.GetMapping
    @Operation(summary = "商品公开评价分页")
    public R<com.lemnt.mall.adapter.app.product.res.CommentPageRes> page(
            @Valid @org.springframework.web.bind.annotation.ModelAttribute com.lemnt.mall.adapter.app.product.req.CommentPageReq req) {
        var result = commentPublicPageQueryExe.execute(CommentReqConverter.page(req));
        return R.page(result.getRecords().stream().map(CommentResConverter::page).toList(), result.getTotal());
    }

    /** 只允许本人完成订单的SKU，令牌由网关统一校验。 */
    @PostMapping
    @Operation(summary = "评价本人已完成订单内商品")
    public R<CommentCreateRes> create(@Valid @RequestBody CommentCreateReq req) {
        return R.ok(CommentResConverter.created(commentCreateCmdExe.execute(CommentReqConverter.create(SecurityUtils.getUserId(),
            req))));
    }
}
