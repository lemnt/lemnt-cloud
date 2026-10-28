package com.lemnt.mall.adapter.web.promotion;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.mall.adapter.web.promotion.converter.DiscountActivityReqConverter;
import com.lemnt.mall.adapter.web.promotion.converter.DiscountActivityResConverter;
import com.lemnt.mall.adapter.web.promotion.req.DiscountActivityCreateReq;
import com.lemnt.mall.adapter.web.promotion.req.DiscountActivityPageReq;
import com.lemnt.mall.adapter.web.promotion.req.DiscountActivityUpdateReq;
import com.lemnt.mall.adapter.web.promotion.res.DiscountActivityDetailRes;
import com.lemnt.mall.adapter.web.promotion.res.DiscountActivityPageRes;
import com.lemnt.mall.application.promotion.executor.DiscountActivityCloseCmdExe;
import com.lemnt.mall.application.promotion.executor.DiscountActivityCreateCmdExe;
import com.lemnt.mall.application.promotion.executor.DiscountActivityDeleteCmdExe;
import com.lemnt.mall.application.promotion.executor.DiscountActivityDetailQueryExe;
import com.lemnt.mall.application.promotion.executor.DiscountActivityPageQueryExe;
import com.lemnt.mall.application.promotion.executor.DiscountActivityUpdateCmdExe;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.DeleteMapping;
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

/** 折扣活动直接管理入口。 */
@RestController
@RequestMapping("/mall/promotion/discount-activities")
@Tag(name = "折扣活动")
@RequiredArgsConstructor
public class DiscountActivityController {
    private final DiscountActivityCreateCmdExe create;
    private final DiscountActivityUpdateCmdExe update;
    private final DiscountActivityCloseCmdExe close;
    private final DiscountActivityDeleteCmdExe delete;
    private final DiscountActivityDetailQueryExe detail;
    private final DiscountActivityPageQueryExe page;

    @PostMapping
    @Operation(summary = "创建折扣活动")
    @RequiresPermissions("mall:promotion:create")
    public R<Long> create(@Valid @RequestBody DiscountActivityCreateReq req) {
        return R.ok(create.execute(DiscountActivityReqConverter.create(req)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "修改折扣活动")
    @RequiresPermissions("mall:promotion:update")
    public R<Boolean> update(@PathVariable Long id, @Valid @RequestBody DiscountActivityUpdateReq req) {
        return R.ok(update.execute(DiscountActivityReqConverter.update(id, req)));
    }

    @PutMapping("/{id}/close")
    @Operation(summary = "关闭折扣活动，关闭后不可重新开启")
    @RequiresPermissions("mall:promotion:update")
    public R<Boolean> close(@PathVariable Long id) {
        return R.ok(close.execute(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除已关闭折扣活动")
    @RequiresPermissions("mall:promotion:delete")
    public R<Boolean> delete(@PathVariable Long id) {
        return R.ok(delete.execute(id));
    }

    @GetMapping("/{id}")
    @Operation(summary = "折扣活动详情")
    @RequiresPermissions("mall:promotion:query")
    public R<DiscountActivityDetailRes> detail(@PathVariable Long id) {
        return R.ok(DiscountActivityResConverter.detail(detail.execute(id)));
    }

    @GetMapping
    @Operation(summary = "折扣活动分页")
    @RequiresPermissions("mall:promotion:list")
    public R<DiscountActivityPageRes> page(@Valid @ModelAttribute DiscountActivityPageReq req) {
        var result = page.execute(DiscountActivityReqConverter.page(req));
        return R.page(result.getRecords().stream().map(DiscountActivityResConverter::page).toList(), result.getTotal());
    }
}
