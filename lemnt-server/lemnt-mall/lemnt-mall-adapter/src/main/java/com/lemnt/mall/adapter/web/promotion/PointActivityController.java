package com.lemnt.mall.adapter.web.promotion;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.mall.adapter.web.promotion.converter.PointConverter;
import com.lemnt.mall.adapter.web.promotion.req.PointActivityCreateReq;
import com.lemnt.mall.adapter.web.promotion.req.PointActivityPageReq;
import com.lemnt.mall.adapter.web.promotion.req.PointActivityUpdateReq;
import com.lemnt.mall.adapter.web.promotion.res.PointActivityRes;
import com.lemnt.mall.application.promotion.executor.PointActivityCloseCmdExe;
import com.lemnt.mall.application.promotion.executor.PointActivityCreateCmdExe;
import com.lemnt.mall.application.promotion.executor.PointActivityDeleteCmdExe;
import com.lemnt.mall.application.promotion.executor.PointActivityDetailQueryExe;
import com.lemnt.mall.application.promotion.executor.PointActivityPageQueryExe;
import com.lemnt.mall.application.promotion.executor.PointActivityUpdateCmdExe;

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

/** 积分商城活动管理入口。 */
@RestController
@RequestMapping("/mall/promotion/point-activities")
@Tag(name = "积分商城活动")
@RequiredArgsConstructor
public class PointActivityController {
    private final PointActivityCreateCmdExe create;
    private final PointActivityUpdateCmdExe update;
    private final PointActivityCloseCmdExe close;
    private final PointActivityDeleteCmdExe delete;
    private final PointActivityDetailQueryExe detail;
    private final PointActivityPageQueryExe page;

    @PostMapping
    @Operation(summary = "创建积分商城活动")
    @RequiresPermissions("mall:promotion:create")
    public R<Long> create(@Valid @RequestBody PointActivityCreateReq req) {
        return R.ok(create.execute(PointConverter.create(req)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "修改积分商城活动")
    @RequiresPermissions("mall:promotion:update")
    public R<Boolean> update(@PathVariable Long id, @Valid @RequestBody PointActivityUpdateReq req) {
        return R.ok(update.execute(PointConverter.update(id, req)));
    }

    @PutMapping("/{id}/close")
    @Operation(summary = "关闭积分商城活动")
    @RequiresPermissions("mall:promotion:update")
    public R<Boolean> close(@PathVariable Long id) {
        return R.ok(close.execute(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除已关闭积分商城活动")
    @RequiresPermissions("mall:promotion:delete")
    public R<Boolean> delete(@PathVariable Long id) {
        return R.ok(delete.execute(id));
    }

    @GetMapping("/{id}")
    @Operation(summary = "积分商城活动详情")
    @RequiresPermissions("mall:promotion:query")
    public R<PointActivityRes> detail(@PathVariable Long id) {
        return R.ok(PointConverter.activity(detail.execute(id)));
    }

    @GetMapping
    @Operation(summary = "积分商城活动分页")
    @RequiresPermissions("mall:promotion:list")
    public R<PointActivityRes> page(@Valid @ModelAttribute PointActivityPageReq req) {
        var result = page.execute(PointConverter.page(req));
        return R.page(result.getRecords().stream().map(PointConverter::activity).toList(),
                result.getTotal());
    }
}
