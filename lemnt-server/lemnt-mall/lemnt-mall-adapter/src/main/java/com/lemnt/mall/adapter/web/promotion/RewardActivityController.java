package com.lemnt.mall.adapter.web.promotion;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.mall.adapter.web.promotion.converter.RewardActivityReqConverter;
import com.lemnt.mall.adapter.web.promotion.converter.RewardActivityResConverter;
import com.lemnt.mall.adapter.web.promotion.req.RewardActivityCreateReq;
import com.lemnt.mall.adapter.web.promotion.req.RewardActivityPageReq;
import com.lemnt.mall.adapter.web.promotion.req.RewardActivityUpdateReq;
import com.lemnt.mall.adapter.web.promotion.res.RewardActivityDetailRes;
import com.lemnt.mall.adapter.web.promotion.res.RewardActivityPageRes;
import com.lemnt.mall.application.promotion.executor.RewardActivityCloseCmdExe;
import com.lemnt.mall.application.promotion.executor.RewardActivityCreateCmdExe;
import com.lemnt.mall.application.promotion.executor.RewardActivityDeleteCmdExe;
import com.lemnt.mall.application.promotion.executor.RewardActivityDetailQueryExe;
import com.lemnt.mall.application.promotion.executor.RewardActivityPageQueryExe;
import com.lemnt.mall.application.promotion.executor.RewardActivityUpdateCmdExe;

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

/** 满减活动直接管理入口。 */
@RestController
@RequestMapping("/mall/promotion/reward-activities")
@Tag(name = "满减活动")
@RequiredArgsConstructor
public class RewardActivityController {
    private final RewardActivityCreateCmdExe create;
    private final RewardActivityUpdateCmdExe update;
    private final RewardActivityCloseCmdExe close;
    private final RewardActivityDeleteCmdExe delete;
    private final RewardActivityDetailQueryExe detail;
    private final RewardActivityPageQueryExe page;

    @PostMapping
    @Operation(summary = "创建满减活动")
    @RequiresPermissions("mall:promotion:create")
    public R<Long> create(@Valid @RequestBody RewardActivityCreateReq req) {
        return R.ok(create.execute(RewardActivityReqConverter.create(req)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "修改满减活动")
    @RequiresPermissions("mall:promotion:update")
    public R<Boolean> update(@PathVariable Long id, @Valid @RequestBody RewardActivityUpdateReq req) {
        return R.ok(update.execute(RewardActivityReqConverter.update(id, req)));
    }

    @PutMapping("/{id}/close")
    @Operation(summary = "关闭满减活动，关闭后不可重新开启")
    @RequiresPermissions("mall:promotion:update")
    public R<Boolean> close(@PathVariable Long id) {
        return R.ok(close.execute(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除已关闭满减活动")
    @RequiresPermissions("mall:promotion:delete")
    public R<Boolean> delete(@PathVariable Long id) {
        return R.ok(delete.execute(id));
    }

    @GetMapping("/{id}")
    @Operation(summary = "满减活动详情")
    @RequiresPermissions("mall:promotion:query")
    public R<RewardActivityDetailRes> detail(@PathVariable Long id) {
        return R.ok(RewardActivityResConverter.detail(detail.execute(id)));
    }

    @GetMapping
    @Operation(summary = "满减活动分页")
    @RequiresPermissions("mall:promotion:list")
    public R<RewardActivityPageRes> page(@Valid @ModelAttribute RewardActivityPageReq req) {
        var result = page.execute(RewardActivityReqConverter.page(req));
        return R.page(result.getRecords().stream().map(RewardActivityResConverter::page).toList(), result.getTotal());
    }
}
