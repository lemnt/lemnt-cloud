package com.lemnt.mall.adapter.app.store;

import com.lemnt.common.core.domain.R;
import com.lemnt.mall.adapter.app.store.converter.StoreResConverter;
import com.lemnt.mall.adapter.app.store.res.StoreCategoryRes;
import com.lemnt.mall.application.store.executor.StoreCategoryListQueryExe;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import java.util.List;

/** 小程序商品分类接口。 */
@Tag(name = "小程序商品分类")
@RestController
@RequestMapping("/mall/app")
@RequiredArgsConstructor
public class AppCategoryController {
    private final StoreCategoryListQueryExe storeCategoryListQueryExe;

    @GetMapping("/categories")
    @Operation(summary = "公开启用分类")
    public R<List<StoreCategoryRes>> categories() {
        return R.ok(storeCategoryListQueryExe.execute().stream().map(StoreResConverter::category).toList());
    }
}
