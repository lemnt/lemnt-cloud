package com.lemnt.mall.adapter.web.promotion;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.mall.adapter.web.promotion.converter.SeckillActivityConverter;
import com.lemnt.mall.adapter.web.promotion.req.SeckillActivityCreateReq;
import com.lemnt.mall.adapter.web.promotion.req.SeckillActivityPageReq;
import com.lemnt.mall.adapter.web.promotion.req.SeckillActivityUpdateReq;
import com.lemnt.mall.adapter.web.promotion.res.SeckillActivityRes;
import com.lemnt.mall.application.promotion.executor.SeckillActivityCloseCmdExe;
import com.lemnt.mall.application.promotion.executor.SeckillActivityCreateCmdExe;
import com.lemnt.mall.application.promotion.executor.SeckillActivityDeleteCmdExe;
import com.lemnt.mall.application.promotion.executor.SeckillActivityDetailQueryExe;
import com.lemnt.mall.application.promotion.executor.SeckillActivityPageQueryExe;
import com.lemnt.mall.application.promotion.executor.SeckillActivityUpdateCmdExe;

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

/** 秒杀活动管理入口。 */
@RestController
@RequestMapping("/mall/promotion/seckill-activities")
@Tag(name = "秒杀活动")
@RequiredArgsConstructor
public class SeckillActivityController {
    private final SeckillActivityCreateCmdExe create;
    private final SeckillActivityUpdateCmdExe update;
    private final SeckillActivityCloseCmdExe close;
    private final SeckillActivityDeleteCmdExe delete;
    private final SeckillActivityDetailQueryExe detail;
    private final SeckillActivityPageQueryExe page;

    @PostMapping
    @Operation(summary = "创建秒杀活动")
    @RequiresPermissions("mall:promotion:create")
    public R<Long> create(@Valid @RequestBody SeckillActivityCreateReq req) {
        return R.ok(create.execute(SeckillActivityConverter.create(req)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "修改秒杀活动")
    @RequiresPermissions("mall:promotion:update")
    public R<Boolean> update(@PathVariable Long id, @Valid @RequestBody SeckillActivityUpdateReq req) {
        return R.ok(update.execute(SeckillActivityConverter.update(id, req)));
    }

    @PutMapping("/{id}/close")
    @Operation(summary = "关闭秒杀活动，关闭后不可重新开启")
    @RequiresPermissions("mall:promotion:update")
    public R<Boolean> close(@PathVariable Long id) {
        return R.ok(close.execute(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除已关闭秒杀活动")
    @RequiresPermissions("mall:promotion:delete")
    public R<Boolean> delete(@PathVariable Long id) {
        return R.ok(delete.execute(id));
    }

    @GetMapping("/{id}")
    @Operation(summary = "秒杀活动详情")
    @RequiresPermissions("mall:promotion:query")
    public R<SeckillActivityRes> detail(@PathVariable Long id) {
        return R.ok(SeckillActivityConverter.res(detail.execute(id)));
    }

    @GetMapping
    @Operation(summary = "秒杀活动分页")
    @RequiresPermissions("mall:promotion:list")
    public R<SeckillActivityRes> page(@Valid @ModelAttribute SeckillActivityPageReq req) {
        var result = page.execute(SeckillActivityConverter.page(req));
        return R.page(result.getRecords().stream().map(SeckillActivityConverter::res).toList(), result.getTotal());
    }
}
