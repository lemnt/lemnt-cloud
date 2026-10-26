package com.lemnt.trade.adapter.web.promotion;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.trade.adapter.web.promotion.req.*;
import com.lemnt.trade.adapter.web.promotion.res.*;
import com.lemnt.trade.adapter.web.promotion.converter.*;
import com.lemnt.trade.application.promotion.executor.*;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/trade/admin/promotions")
@Tag(name = "订单促销活动管理")
@lombok.RequiredArgsConstructor
public class PromotionAdminController {
    private final PromotionAdminAccess access;
    private final PromotionCreateCmdExe create;
    private final PromotionDraftSaveCmdExe save;
    private final PromotionPublishCmdExe publish;
    private final PromotionStateChangeCmdExe state;
    private final PromotionDetailQueryExe detail;
    private final PromotionPageQueryExe page;

    @PostMapping
    @Operation(summary = "创建活动草稿")
    @RequiresPermissions("trade:promotion:create")
    public R<PromotionResultRes> create(@Valid @RequestBody PromotionCreateReq req) {
        long operator = access.operator("trade:promotion:create");
        return R.ok(
                PromotionResConverter.result(
                        create.execute(PromotionReqConverter.create(operator, req))));
    }

    @PutMapping("/{id}/draft")
    @Operation(summary = "保存完整活动草稿")
    @RequiresPermissions("trade:promotion:update")
    public R<PromotionResultRes> save(
            @PathVariable String id, @Valid @RequestBody PromotionDraftSaveReq req) {
        long operator = access.operator("trade:promotion:update");
        return R.ok(
                PromotionResConverter.result(
                        save.execute(PromotionReqConverter.save(operator, id, req))));
    }

    @PostMapping("/{id}/publish")
    @Operation(summary = "发布不可变活动版本")
    @RequiresPermissions("trade:promotion:publish")
    public R<PromotionResultRes> publish(
            @PathVariable String id, @Valid @RequestBody PromotionPublishReq req) {
        long operator = access.operator("trade:promotion:publish");
        return R.ok(
                PromotionResConverter.result(
                        publish.execute(PromotionReqConverter.publish(operator, id, req))));
    }

    @PutMapping("/{id}/state")
    @Operation(summary = "暂停、恢复或归档活动")
    @RequiresPermissions("trade:promotion:state")
    public R<PromotionResultRes> state(
            @PathVariable String id, @Valid @RequestBody PromotionStateChangeReq req) {
        long operator = access.operator("trade:promotion:state");
        return R.ok(
                PromotionResConverter.result(
                        state.execute(PromotionReqConverter.state(operator, id, req))));
    }

    @GetMapping("/{id}")
    @Operation(summary = "活动草稿及当前发布版本详情")
    @RequiresPermissions("trade:promotion:query")
    public R<PromotionDetailRes> detail(@PathVariable String id) {
        access.operator("trade:promotion:query");
        return R.ok(PromotionResConverter.detail(detail.execute(PromotionReqConverter.id(id))));
    }

    @GetMapping
    @Operation(summary = "活动分页摘要")
    @RequiresPermissions("trade:promotion:list")
    public R<PromotionPageRes> page(
            @Valid @ModelAttribute PromotionPageReq req, HttpServletRequest request) {
        access.operator("trade:promotion:list");
        return R.ok(
                PromotionResConverter.page(
                        page.execute(PromotionReqConverter.page(req, request.getParameterMap()))));
    }
}
