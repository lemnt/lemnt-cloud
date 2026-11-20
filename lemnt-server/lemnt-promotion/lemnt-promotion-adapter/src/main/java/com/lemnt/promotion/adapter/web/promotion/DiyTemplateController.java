package com.lemnt.promotion.adapter.web.promotion;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.promotion.adapter.web.promotion.converter.DiyConverter;
import com.lemnt.promotion.adapter.web.promotion.req.DiyPropertyReq;
import com.lemnt.promotion.adapter.web.promotion.req.DiyTemplatePageReq;
import com.lemnt.promotion.adapter.web.promotion.req.DiyTemplateSaveReq;
import com.lemnt.promotion.adapter.web.promotion.res.DiyTemplateRes;
import com.lemnt.promotion.application.promotion.cmd.DiyPropertyCmd;
import com.lemnt.promotion.application.promotion.executor.DiyTemplateCreateCmdExe;
import com.lemnt.promotion.application.promotion.executor.DiyTemplateDeleteCmdExe;
import com.lemnt.promotion.application.promotion.executor.DiyTemplateDetailQueryExe;
import com.lemnt.promotion.application.promotion.executor.DiyTemplatePageQueryExe;
import com.lemnt.promotion.application.promotion.executor.DiyTemplatePropertyCmdExe;
import com.lemnt.promotion.application.promotion.executor.DiyTemplateUpdateCmdExe;
import com.lemnt.promotion.application.promotion.executor.DiyTemplateUseCmdExe;

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

/**
 * 后台装修模板管理。
 */
@Tag(name = "装修模板")
@RestController
@RequestMapping("/mall/promotion/diy-templates")
@RequiredArgsConstructor
public class DiyTemplateController {
    private final DiyTemplateCreateCmdExe create;
    private final DiyTemplateUpdateCmdExe update;
    private final DiyTemplateUseCmdExe use;
    private final DiyTemplateDeleteCmdExe delete;
    private final DiyTemplateDetailQueryExe detail;
    private final DiyTemplatePageQueryExe page;
    private final DiyTemplatePropertyCmdExe property;

    @GetMapping
    @Operation(summary = "装修模板分页")
    @RequiresPermissions("mall:promotion:list")
    public R<DiyTemplateRes> page(@Valid @ModelAttribute DiyTemplatePageReq req) {
        var result = page.execute(DiyConverter.page(req));
        return R.page(
            result.getRecords().stream().map(DiyConverter::template).toList(),
            result.getTotal());
    }

    @GetMapping("/{id}")
    @Operation(summary = "装修模板详情")
    @RequiresPermissions("mall:promotion:query")
    public R<DiyTemplateRes> detail(@PathVariable Long id) {
        return R.ok(DiyConverter.template(detail.execute(id)));
    }

    @PostMapping
    @Operation(summary = "新增装修模板")
    @RequiresPermissions("mall:promotion:create")
    public R<Long> create(@Valid @RequestBody DiyTemplateSaveReq req) {
        return R.ok(create.execute(DiyConverter.template(req)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "修改装修模板")
    @RequiresPermissions("mall:promotion:update")
    public R<Boolean> update(@PathVariable Long id, @Valid @RequestBody DiyTemplateSaveReq req) {
        return R.ok(update.execute(DiyConverter.template(id, req)));
    }

    @PutMapping("/{id}/use")
    @Operation(summary = "启用装修模板")
    @RequiresPermissions("mall:promotion:update")
    public R<Boolean> use(@PathVariable Long id) {
        return R.ok(use.execute(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除装修模板")
    @RequiresPermissions("mall:promotion:delete")
    public R<Boolean> delete(@PathVariable Long id) {
        return R.ok(delete.execute(id));
    }

    @PutMapping("/{id}/property")
    @Operation(summary = "更新模板属性")
    @RequiresPermissions("mall:promotion:update")
    public R<Boolean> property(@PathVariable Long id, @Valid @RequestBody DiyPropertyReq req) {
        return R.ok(property.execute(new DiyPropertyCmd(id, req.property())));
    }
}
