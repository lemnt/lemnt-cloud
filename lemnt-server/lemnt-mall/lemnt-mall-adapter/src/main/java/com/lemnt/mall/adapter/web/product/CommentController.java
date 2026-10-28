package com.lemnt.mall.adapter.web.product;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.common.security.utils.SecurityUtils;
import com.lemnt.mall.adapter.web.product.converter.CommentReqConverter;
import com.lemnt.mall.adapter.web.product.converter.CommentResConverter;
import com.lemnt.mall.adapter.web.product.req.CommentPageReq;
import com.lemnt.mall.adapter.web.product.req.CommentReplyReq;
import com.lemnt.mall.adapter.web.product.req.CommentVisibleReq;
import com.lemnt.mall.adapter.web.product.res.CommentDetailRes;
import com.lemnt.mall.adapter.web.product.res.CommentPageRes;
import com.lemnt.mall.adapter.web.product.res.CommentReplyRes;
import com.lemnt.mall.application.product.executor.CommentDetailQueryExe;
import com.lemnt.mall.application.product.executor.CommentPageQueryExe;
import com.lemnt.mall.application.product.executor.CommentReplyCmdExe;
import com.lemnt.mall.application.product.executor.CommentVisibleCmdExe;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import jakarta.validation.Valid;

/** 后台商品评价管理。 */
@Tag(name = "商品评价管理")
@RestController
@RequestMapping("/mall/comments")
@RequiredArgsConstructor
public class CommentController {
    private final CommentPageQueryExe commentPageQueryExe;
    private final CommentDetailQueryExe commentDetailQueryExe;
    private final CommentReplyCmdExe commentReplyCmdExe;
    private final CommentVisibleCmdExe commentVisibleCmdExe;

    /** 分页查询含隐藏内容的后台评价。 */
    @GetMapping
    @Operation(summary = "商品评价分页")
    @RequiresPermissions("mall:comment:list")
    public R<CommentPageRes> page(@Valid @ModelAttribute CommentPageReq req) {
        var value = commentPageQueryExe.execute(CommentReqConverter.page(req));
        return R.page(value.getRecords().stream().map(CommentResConverter::page).toList(), value.getTotal());
    }

    /** 后台评价详情。 */
    @GetMapping("/{id}")
    @Operation(summary = "商品评价详情")
    @RequiresPermissions("mall:comment:query")
    public R<CommentDetailRes> detail(@PathVariable Long id) {
        var value = commentDetailQueryExe.execute(id);
        return value == null ? R.fail("评价不存在") : R.ok(CommentResConverter.detail(value));
    }

    /** 商家回复只使用当前管理员身份。 */
    @PutMapping("/{id}/reply")
    @Operation(summary = "商家回复评价")
    @RequiresPermissions("mall:comment:reply")
    public R<CommentReplyRes> reply(@PathVariable Long id, @Valid @RequestBody CommentReplyReq req) {
        return R.ok(CommentResConverter.reply(commentReplyCmdExe.execute(CommentReqConverter.reply(id, SecurityUtils.getUserId(),
            req))));
    }

    /** 设置显示或隐藏。 */
    @PutMapping("/{id}/visible")
    @Operation(summary = "显示或隐藏商品评价")
    @RequiresPermissions("mall:comment:edit")
    public R<Boolean> visible(@PathVariable Long id, @Valid @RequestBody CommentVisibleReq req) {
        return R.ok(commentVisibleCmdExe.execute(CommentReqConverter.visible(id, req)));
    }
}
