package com.lemnt.mall.adapter.web.promotion;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.mall.adapter.web.promotion.converter.BargainConverter;
import com.lemnt.mall.adapter.web.promotion.req.BargainActivityCreateReq;
import com.lemnt.mall.adapter.web.promotion.req.BargainActivityPageReq;
import com.lemnt.mall.adapter.web.promotion.req.BargainActivityUpdateReq;
import com.lemnt.mall.adapter.web.promotion.res.BargainActivityRes;
import com.lemnt.mall.application.promotion.executor.BargainActivityCloseCmdExe;
import com.lemnt.mall.application.promotion.executor.BargainActivityCreateCmdExe;
import com.lemnt.mall.application.promotion.executor.BargainActivityDeleteCmdExe;
import com.lemnt.mall.application.promotion.executor.BargainActivityDetailQueryExe;
import com.lemnt.mall.application.promotion.executor.BargainActivityPageQueryExe;
import com.lemnt.mall.application.promotion.executor.BargainActivityUpdateCmdExe;

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

/** 砍价活动管理入口。 */
@RestController
@RequestMapping("/mall/promotion/bargain-activities")
@Tag(name = "砍价活动")
@RequiredArgsConstructor
public class BargainActivityController {
    private final BargainActivityCreateCmdExe create;
    private final BargainActivityUpdateCmdExe update;
    private final BargainActivityCloseCmdExe close;
    private final BargainActivityDeleteCmdExe delete;
    private final BargainActivityDetailQueryExe detail;
    private final BargainActivityPageQueryExe page;

    @PostMapping
    @Operation(summary = "创建砍价活动")
    @RequiresPermissions("mall:promotion:create")
    public R<Long> create(@Valid @RequestBody BargainActivityCreateReq req) {
        return R.ok(create.execute(BargainConverter.create(req)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "修改砍价活动")
    @RequiresPermissions("mall:promotion:update")
    public R<Boolean> update(@PathVariable Long id, @Valid @RequestBody BargainActivityUpdateReq req) {
        return R.ok(update.execute(BargainConverter.update(id, req)));
    }

    @PutMapping("/{id}/close")
    @Operation(summary = "关闭砍价活动")
    @RequiresPermissions("mall:promotion:update")
    public R<Boolean> close(@PathVariable Long id) {
        return R.ok(close.execute(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除已关闭砍价活动")
    @RequiresPermissions("mall:promotion:delete")
    public R<Boolean> delete(@PathVariable Long id) {
        return R.ok(delete.execute(id));
    }

    @GetMapping("/{id}")
    @Operation(summary = "砍价活动详情")
    @RequiresPermissions("mall:promotion:query")
    public R<BargainActivityRes> detail(@PathVariable Long id) {
        return R.ok(BargainConverter.activity(detail.execute(id)));
    }

    @GetMapping
    @Operation(summary = "砍价活动分页")
    @RequiresPermissions("mall:promotion:list")
    public R<BargainActivityRes> page(@Valid @ModelAttribute BargainActivityPageReq req) {
        var result = page.execute(BargainConverter.page(req));
        return R.page(result.getRecords().stream().map(BargainConverter::activity).toList(),
                result.getTotal());
    }
}
