package com.lemnt.promotion.adapter.web.promotion;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.promotion.adapter.web.promotion.converter.DiyConverter;
import com.lemnt.promotion.adapter.web.promotion.req.DiyPagePageReq;
import com.lemnt.promotion.adapter.web.promotion.req.DiyPageSaveReq;
import com.lemnt.promotion.adapter.web.promotion.req.DiyPropertyReq;
import com.lemnt.promotion.adapter.web.promotion.res.DiyPageRes;
import com.lemnt.promotion.application.promotion.cmd.DiyPropertyCmd;
import com.lemnt.promotion.application.promotion.executor.DiyPageCreateCmdExe;
import com.lemnt.promotion.application.promotion.executor.DiyPageDeleteCmdExe;
import com.lemnt.promotion.application.promotion.executor.DiyPageDetailQueryExe;
import com.lemnt.promotion.application.promotion.executor.DiyPagePageQueryExe;
import com.lemnt.promotion.application.promotion.executor.DiyPagePropertyCmdExe;
import com.lemnt.promotion.application.promotion.executor.DiyPageUpdateCmdExe;

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

/** 后台装修页面管理。 */
@Tag(name = "装修页面")
@RestController
@RequestMapping("/mall/promotion/diy-pages")
@RequiredArgsConstructor
public class DiyPageController {
    private final DiyPageCreateCmdExe create;
    private final DiyPageUpdateCmdExe update;
    private final DiyPageDeleteCmdExe delete;
    private final DiyPageDetailQueryExe detail;
    private final DiyPagePageQueryExe page;
    private final DiyPagePropertyCmdExe property;

    @GetMapping
    @Operation(summary = "装修页面分页")
    @RequiresPermissions("mall:promotion:list")
    public R<DiyPageRes> page(@Valid @ModelAttribute DiyPagePageReq req) {
        var result = page.execute(DiyConverter.page(req));
        return R.page(
                result.getRecords().stream().map(DiyConverter::page).toList(), result.getTotal());
    }

    @GetMapping("/{id}")
    @Operation(summary = "装修页面详情")
    @RequiresPermissions("mall:promotion:query")
    public R<DiyPageRes> detail(@PathVariable Long id) {
        return R.ok(DiyConverter.page(detail.execute(id)));
    }

    @PostMapping
    @Operation(summary = "新增装修页面")
    @RequiresPermissions("mall:promotion:create")
    public R<Long> create(@Valid @RequestBody DiyPageSaveReq req) {
        return R.ok(create.execute(DiyConverter.page(req)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "修改装修页面")
    @RequiresPermissions("mall:promotion:update")
    public R<Boolean> update(@PathVariable Long id, @Valid @RequestBody DiyPageSaveReq req) {
        return R.ok(update.execute(DiyConverter.page(id, req)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除装修页面")
    @RequiresPermissions("mall:promotion:delete")
    public R<Boolean> delete(@PathVariable Long id) {
        return R.ok(delete.execute(id));
    }

    @PutMapping("/{id}/property")
    @Operation(summary = "更新页面属性")
    @RequiresPermissions("mall:promotion:update")
    public R<Boolean> property(@PathVariable Long id, @Valid @RequestBody DiyPropertyReq req) {
        return R.ok(property.execute(new DiyPropertyCmd(id, req.property())));
    }
}
