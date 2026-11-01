package com.lemnt.mall.adapter.app.promotion;

import com.lemnt.common.core.domain.R;
import com.lemnt.mall.adapter.app.promotion.converter.AppDiyConverter;
import com.lemnt.mall.adapter.app.promotion.res.AppDiyPageRes;
import com.lemnt.mall.adapter.app.promotion.res.AppDiyTemplateRes;
import com.lemnt.mall.application.promotion.executor.AppDiyPageQueryExe;
import com.lemnt.mall.application.promotion.executor.AppDiyTemplateQueryExe;
import com.lemnt.mall.application.promotion.executor.AppDiyUsedTemplateQueryExe;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

/** 小程序装修模板与页面。 */
@Tag(name = "小程序装修")
@RestController
@RequestMapping("/mall/app/diy")
@RequiredArgsConstructor
public class AppDiyController {
    private final AppDiyUsedTemplateQueryExe used;
    private final AppDiyTemplateQueryExe template;
    private final AppDiyPageQueryExe page;

    @GetMapping("/template/used")
    @Operation(summary = "使用中的装修模板")
    public R<AppDiyTemplateRes> used() {
        return R.ok(AppDiyConverter.template(used.execute()));
    }

    @GetMapping("/template/{id}")
    @Operation(summary = "指定装修模板")
    public R<AppDiyTemplateRes> template(@PathVariable Long id) {
        return R.ok(AppDiyConverter.template(template.execute(id)));
    }

    @GetMapping("/page/{id}")
    @Operation(summary = "装修页面")
    public R<AppDiyPageRes> page(@PathVariable Long id) {
        return R.ok(AppDiyConverter.page(page.execute(id)));
    }
}
