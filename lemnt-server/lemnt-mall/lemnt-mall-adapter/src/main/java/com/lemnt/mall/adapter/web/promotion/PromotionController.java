package com.lemnt.mall.adapter.web.promotion;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.mall.adapter.web.promotion.converter.PromotionReqConverter;
import com.lemnt.mall.adapter.web.promotion.converter.PromotionResConverter;
import com.lemnt.mall.adapter.web.promotion.req.PromotionCreateReq;
import com.lemnt.mall.adapter.web.promotion.req.PromotionDraftSaveReq;
import com.lemnt.mall.adapter.web.promotion.req.PromotionPageReq;
import com.lemnt.mall.adapter.web.promotion.req.PromotionPublishReq;
import com.lemnt.mall.adapter.web.promotion.req.PromotionStateChangeReq;
import com.lemnt.mall.adapter.web.promotion.res.MallPromotionCreateRes;
import com.lemnt.mall.adapter.web.promotion.res.MallPromotionPublishRes;
import com.lemnt.mall.adapter.web.promotion.res.MallPromotionSaveRes;
import com.lemnt.mall.adapter.web.promotion.res.MallPromotionStateRes;
import com.lemnt.mall.adapter.web.promotion.res.PromotionDetailRes;
import com.lemnt.mall.adapter.web.promotion.res.PromotionPageItemRes;
import com.lemnt.mall.application.promotion.executor.PromotionCreateCmdExe;
import com.lemnt.mall.application.promotion.executor.PromotionDetailQueryExe;
import com.lemnt.mall.application.promotion.executor.PromotionDraftSaveCmdExe;
import com.lemnt.mall.application.promotion.executor.PromotionPageQueryExe;
import com.lemnt.mall.application.promotion.executor.PromotionPublishCmdExe;
import com.lemnt.mall.application.promotion.executor.PromotionStateChangeCmdExe;

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

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

/**
 * 订单促销活动管理。
 */
@RestController
@RequestMapping("/mall/admin/promotions")
@Tag(name = "订单促销活动管理")
@RequiredArgsConstructor
public class PromotionController {
    private final PromotionAccess access;
    private final PromotionCreateCmdExe promotionCreateCmdExe;
    private final PromotionDraftSaveCmdExe promotionDraftSaveCmdExe;
    private final PromotionPublishCmdExe promotionPublishCmdExe;
    private final PromotionStateChangeCmdExe promotionStateChangeCmdExe;
    private final PromotionDetailQueryExe promotionDetailQueryExe;
    private final PromotionPageQueryExe promotionPageQueryExe;

    @PostMapping
    @Operation(summary = "创建活动草稿")
    @RequiresPermissions("mall:promotion:create")
    public R<MallPromotionCreateRes> create(@Valid @RequestBody PromotionCreateReq req) {
        long operator = access.operator("mall:promotion:create");
        return R.ok(
                PromotionResConverter.resultToMallPromotionCreateRes(
                promotionCreateCmdExe.execute(PromotionReqConverter.create(operator, req))));
    }

    @PutMapping("/{id}/draft")
    @Operation(summary = "保存完整活动草稿")
    @RequiresPermissions("mall:promotion:update")
    public R<MallPromotionSaveRes> save(
            @PathVariable String id, @Valid @RequestBody PromotionDraftSaveReq req) {
        long operator = access.operator("mall:promotion:update");
        return R.ok(
                PromotionResConverter.resultToMallPromotionSaveRes(
                promotionDraftSaveCmdExe.execute(PromotionReqConverter.save(operator, id, req))));
    }

    @PostMapping("/{id}/publish")
    @Operation(summary = "发布不可变活动版本")
    @RequiresPermissions("mall:promotion:publish")
    public R<MallPromotionPublishRes> publish(
            @PathVariable String id, @Valid @RequestBody PromotionPublishReq req) {
        long operator = access.operator("mall:promotion:publish");
        return R.ok(
                PromotionResConverter.resultToMallPromotionPublishRes(
                promotionPublishCmdExe.execute(PromotionReqConverter.publish(operator, id, req))));
    }

    @PutMapping("/{id}/state")
    @Operation(summary = "暂停、恢复或归档活动")
    @RequiresPermissions("mall:promotion:state")
    public R<MallPromotionStateRes> state(
            @PathVariable String id, @Valid @RequestBody PromotionStateChangeReq req) {
        long operator = access.operator("mall:promotion:state");
        return R.ok(
                PromotionResConverter.resultToMallPromotionStateRes(
                promotionStateChangeCmdExe.execute(PromotionReqConverter.state(operator, id, req))));
    }

    @GetMapping("/{id}")
    @Operation(summary = "活动草稿及当前发布版本详情")
    @RequiresPermissions("mall:promotion:query")
    public R<PromotionDetailRes> detail(@PathVariable String id) {
        access.operator("mall:promotion:query");
        return R.ok(PromotionResConverter.detail(promotionDetailQueryExe.execute(PromotionReqConverter.id(id))));
    }

    @GetMapping
    @Operation(summary = "活动分页摘要")
    @RequiresPermissions("mall:promotion:list")
    public R<PromotionPageItemRes> page(
            @Valid @ModelAttribute PromotionPageReq req, HttpServletRequest request) {
        access.operator("mall:promotion:list");
        var page = promotionPageQueryExe.execute(PromotionReqConverter.page(req, request.getParameterMap()));
        return R.page(page.records().stream().map(PromotionResConverter::pageItem).toList(), page.total());
    }
}
