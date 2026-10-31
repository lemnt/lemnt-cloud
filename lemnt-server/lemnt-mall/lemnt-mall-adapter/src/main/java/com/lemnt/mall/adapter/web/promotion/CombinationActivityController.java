package com.lemnt.mall.adapter.web.promotion;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.mall.adapter.web.promotion.converter.CombinationConverter;
import com.lemnt.mall.adapter.web.promotion.req.CombinationActivityCreateReq;
import com.lemnt.mall.adapter.web.promotion.req.CombinationActivityPageReq;
import com.lemnt.mall.adapter.web.promotion.req.CombinationActivityUpdateReq;
import com.lemnt.mall.adapter.web.promotion.res.CombinationActivityRes;
import com.lemnt.mall.application.promotion.executor.CombinationActivityCloseCmdExe;
import com.lemnt.mall.application.promotion.executor.CombinationActivityCreateCmdExe;
import com.lemnt.mall.application.promotion.executor.CombinationActivityDeleteCmdExe;
import com.lemnt.mall.application.promotion.executor.CombinationActivityDetailQueryExe;
import com.lemnt.mall.application.promotion.executor.CombinationActivityPageQueryExe;
import com.lemnt.mall.application.promotion.executor.CombinationActivityUpdateCmdExe;

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

/** 拼团活动管理入口。 */
@RestController
@RequestMapping("/mall/promotion/combination-activities")
@Tag(name = "拼团活动")
@RequiredArgsConstructor
public class CombinationActivityController {
    private final CombinationActivityCreateCmdExe create;
    private final CombinationActivityUpdateCmdExe update;
    private final CombinationActivityCloseCmdExe close;
    private final CombinationActivityDeleteCmdExe delete;
    private final CombinationActivityDetailQueryExe detail;
    private final CombinationActivityPageQueryExe page;

    @PostMapping
    @Operation(summary = "创建拼团活动")
    @RequiresPermissions("mall:promotion:create")
    public R<Long> create(@Valid @RequestBody CombinationActivityCreateReq req) {
        return R.ok(create.execute(CombinationConverter.create(req)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "修改拼团活动")
    @RequiresPermissions("mall:promotion:update")
    public R<Boolean> update(@PathVariable Long id, @Valid @RequestBody CombinationActivityUpdateReq req) {
        return R.ok(update.execute(CombinationConverter.update(id, req)));
    }

    @PutMapping("/{id}/close")
    @Operation(summary = "关闭拼团活动，关闭后不可重新开启")
    @RequiresPermissions("mall:promotion:update")
    public R<Boolean> close(@PathVariable Long id) {
        return R.ok(close.execute(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除已关闭拼团活动")
    @RequiresPermissions("mall:promotion:delete")
    public R<Boolean> delete(@PathVariable Long id) {
        return R.ok(delete.execute(id));
    }

    @GetMapping("/{id}")
    @Operation(summary = "拼团活动详情")
    @RequiresPermissions("mall:promotion:query")
    public R<CombinationActivityRes> detail(@PathVariable Long id) {
        return R.ok(CombinationConverter.activity(detail.execute(id)));
    }

    @GetMapping
    @Operation(summary = "拼团活动分页")
    @RequiresPermissions("mall:promotion:list")
    public R<CombinationActivityRes> page(@Valid @ModelAttribute CombinationActivityPageReq req) {
        var result = page.execute(CombinationConverter.page(req));
        return R.page(result.getRecords().stream().map(CombinationConverter::activity).toList(),
                result.getTotal());
    }
}
