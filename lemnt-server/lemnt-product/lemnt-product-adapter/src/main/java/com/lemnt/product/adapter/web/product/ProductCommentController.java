package com.lemnt.product.adapter.web.product;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.common.security.utils.SecurityUtils;
import com.lemnt.product.adapter.web.product.converter.ProductCommentReqConverter;
import com.lemnt.product.adapter.web.product.converter.ProductCommentResConverter;
import com.lemnt.product.adapter.web.product.req.ProductCommentPageReq;
import com.lemnt.product.adapter.web.product.req.ProductCommentReplyReq;
import com.lemnt.product.adapter.web.product.req.ProductCommentSelfCreateReq;
import com.lemnt.product.adapter.web.product.req.ProductCommentVisibleReq;
import com.lemnt.product.adapter.web.product.res.ProductCommentDetailRes;
import com.lemnt.product.adapter.web.product.res.ProductCommentPageRes;
import com.lemnt.product.adapter.web.product.res.ProductCommentReplyRes;
import com.lemnt.product.adapter.web.product.res.ProductCommentSelfCreateRes;
import com.lemnt.product.adapter.web.product.res.ProductCommentVisibleRes;
import com.lemnt.product.application.product.cmd.ProductCommentSelfCreateCmd;
import com.lemnt.product.application.product.executor.ProductCommentDetailQueryExe;
import com.lemnt.product.application.product.executor.ProductCommentPageQueryExe;
import com.lemnt.product.application.product.executor.ProductCommentReplyCmdExe;
import com.lemnt.product.application.product.executor.ProductCommentSelfCreateCmdExe;
import com.lemnt.product.application.product.executor.ProductCommentVisibleCmdExe;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import jakarta.validation.Valid;

/**
 * 后台商品评价管理。
 */
@Tag(name = "商品评价管理")
@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
public class ProductCommentController {
    private final ProductCommentSelfCreateCmdExe commentSelfCreateCmdExe;
    private final ProductCommentPageQueryExe commentPageQueryExe;
    private final ProductCommentDetailQueryExe commentDetailQueryExe;
    private final ProductCommentReplyCmdExe commentReplyCmdExe;
    private final ProductCommentVisibleCmdExe commentVisibleCmdExe;

    /**
     * 后台自评没有会员购买关系，不进入交易评价编排。
     */
    @PostMapping
    @Operation(summary = "添加后台自评")
    @RequiresPermissions("product:comment:create")
    public R<ProductCommentSelfCreateRes> create(@Valid @RequestBody ProductCommentSelfCreateReq req) {
        var id =
            commentSelfCreateCmdExe.execute(
                new ProductCommentSelfCreateCmd(
                    req.userNickname(),
                    req.userAvatar(),
                    req.skuId(),
                    req.descriptionScores(),
                    req.benefitScores(),
                    req.content(),
                    req.picUrls()));
        return R.ok(new ProductCommentSelfCreateRes(id));
    }

    /**
     * 分页查询含隐藏内容的后台评价。
     */
    @GetMapping
    @Operation(summary = "商品评价分页")
    @RequiresPermissions("product:comment:list")
    public R<ProductCommentPageRes> page(@Valid @ModelAttribute ProductCommentPageReq req) {
        var value = commentPageQueryExe.execute(ProductCommentReqConverter.page(req));
        return R.page(
            value.getRecords().stream().map(ProductCommentResConverter::page).toList(),
            value.getTotal());
    }

    /**
     * 后台评价详情。
     */
    @GetMapping("/{id}")
    @Operation(summary = "商品评价详情")
    @RequiresPermissions("product:comment:query")
    public R<ProductCommentDetailRes> detail(@PathVariable Long id) {
        var value = commentDetailQueryExe.execute(id);
        if (value == null) {
            throw new ServiceException("评价不存在", 404);
        }
        return R.ok(ProductCommentResConverter.detail(value));
    }

    /**
     * 商家回复只使用当前管理员身份。
     */
    @PutMapping("/{id}/reply")
    @Operation(summary = "商家回复评价")
    @RequiresPermissions("product:comment:reply")
    public R<ProductCommentReplyRes> reply(
        @PathVariable Long id, @Valid @RequestBody ProductCommentReplyReq req) {
        return R.ok(
            ProductCommentResConverter.reply(
                commentReplyCmdExe.execute(
                    ProductCommentReqConverter.reply(id, SecurityUtils.getUserId(), req))));
    }

    /**
     * 设置显示或隐藏。
     */
    @PutMapping("/{id}/visible")
    @Operation(summary = "显示或隐藏商品评价")
    @RequiresPermissions("product:comment:edit")
    public R<ProductCommentVisibleRes> visible(
        @PathVariable Long id, @Valid @RequestBody ProductCommentVisibleReq req) {
        return R.ok(
            new ProductCommentVisibleRes(
                commentVisibleCmdExe.execute(ProductCommentReqConverter.visible(id, req))));
    }
}
