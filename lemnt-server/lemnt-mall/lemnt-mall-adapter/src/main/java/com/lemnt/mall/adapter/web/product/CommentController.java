package com.lemnt.mall.adapter.web.product;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.mall.adapter.web.product.res.CommentVisibleRes;
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
    private final com.lemnt.mall.application.product.executor.CommentSelfCreateCmdExe commentSelfCreateCmdExe;
    private final CommentPageQueryExe commentPageQueryExe;
    private final CommentDetailQueryExe commentDetailQueryExe;
    private final CommentReplyCmdExe commentReplyCmdExe;
    private final CommentVisibleCmdExe commentVisibleCmdExe;

    /** 后台自评没有会员购买关系，不进入交易评价编排。 */
    @org.springframework.web.bind.annotation.PostMapping
    @Operation(summary = "添加后台自评")
    @RequiresPermissions("mall:comment:create")
    public R<com.lemnt.mall.adapter.web.product.res.CommentSelfCreateRes> create(
            @Valid @RequestBody com.lemnt.mall.adapter.web.product.req.CommentSelfCreateReq req) {
        var id = commentSelfCreateCmdExe.execute(new com.lemnt.mall.application.product.cmd.CommentSelfCreateCmd(
                req.userNickname(), req.userAvatar(), req.skuId(), req.descriptionScores(), req.benefitScores(), req.content(), req.picUrls()));
        return R.ok(new com.lemnt.mall.adapter.web.product.res.CommentSelfCreateRes(id));
    }

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
        if (value == null) {
            throw new ServiceException("评价不存在", 404);
        }
        return R.ok(CommentResConverter.detail(value));
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
    public R<CommentVisibleRes> visible(@PathVariable Long id, @Valid @RequestBody CommentVisibleReq req) {
        return R.ok(new CommentVisibleRes(commentVisibleCmdExe.execute(CommentReqConverter.visible(id, req))));
    }
}
