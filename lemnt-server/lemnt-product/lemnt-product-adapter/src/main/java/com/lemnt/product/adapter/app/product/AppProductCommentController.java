package com.lemnt.product.adapter.app.product;

import com.lemnt.common.core.domain.R;
import com.lemnt.product.adapter.app.product.converter.ProductCommentReqConverter;
import com.lemnt.product.adapter.app.product.converter.ProductCommentResConverter;
import com.lemnt.product.adapter.app.product.req.ProductCommentPageReq;
import com.lemnt.product.adapter.app.product.res.ProductCommentPageRes;
import com.lemnt.product.application.product.executor.ProductCommentPublicPageQueryExe;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

/**
 * 会员商品评价公开查询入口；投稿入口在交易服务（评价资格属订单事实）。
 */
@Tag(name = "会员商品评价")
@RestController
@RequestMapping("/app/comments")
@RequiredArgsConstructor
public class AppProductCommentController {
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
}
