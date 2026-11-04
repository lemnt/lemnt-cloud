package com.lemnt.product.adapter.app.product;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.utils.SecurityUtils;
import com.lemnt.product.adapter.app.product.converter.ProductCommentReqConverter;
import com.lemnt.product.adapter.app.product.converter.ProductCommentResConverter;
import com.lemnt.product.adapter.app.product.req.ProductCommentCreateReq;
import com.lemnt.product.adapter.app.product.req.ProductCommentPageReq;
import com.lemnt.product.adapter.app.product.res.ProductCommentCreateRes;
import com.lemnt.product.adapter.app.product.res.ProductCommentPageRes;
import com.lemnt.product.application.product.executor.ProductCommentCreateCmdExe;
import com.lemnt.product.application.product.executor.ProductCommentPublicPageQueryExe;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import jakarta.validation.Valid;

/**
 * 会员真实订单评价写入入口。
 */
@Tag(name = "会员商品评价")
@RestController
@RequestMapping("/app/comments")
@RequiredArgsConstructor
public class AppProductCommentController {
    private final ProductCommentCreateCmdExe commentCreateCmdExe;
    private final ProductCommentPublicPageQueryExe commentPublicPageQueryExe;

    /**
     * 公开查询当前商品可见评价，不接受其他会员或后台过滤条件。
     */
    @GetMapping
    @Operation(summary = "商品公开评价分页")
    public R<ProductCommentPageRes> page(@Valid @ModelAttribute ProductCommentPageReq req) {
        var result = commentPublicPageQueryExe.execute(ProductCommentReqConverter.page(req));
        return R.page(
            result.getRecords().stream().map(ProductCommentResConverter::page).toList(),
            result.getTotal());
    }

    /**
     * 只允许本人完成订单的SKU，令牌由网关统一校验。
     */
    @PostMapping
    @Operation(summary = "评价本人已完成订单内商品")
    public R<ProductCommentCreateRes> create(@Valid @RequestBody ProductCommentCreateReq req) {
        return R.ok(
            ProductCommentResConverter.created(
                commentCreateCmdExe.execute(
                    ProductCommentReqConverter.create(SecurityUtils.getUserId(), req))));
    }
}
