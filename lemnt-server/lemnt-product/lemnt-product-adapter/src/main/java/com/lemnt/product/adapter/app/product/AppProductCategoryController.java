package com.lemnt.product.adapter.app.product;

import com.lemnt.common.core.domain.R;
import com.lemnt.product.adapter.app.product.converter.ProductCategoryResConverter;
import com.lemnt.product.adapter.app.product.res.AppProductCategorySimpleRes;
import com.lemnt.product.application.product.executor.ProductCategoryEnableListQueryExe;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * 小程序商品分类接口。
 */
@Tag(name = "小程序商品分类")
@RestController
@RequestMapping("/app/categories")
@RequiredArgsConstructor
public class AppProductCategoryController {
    private final ProductCategoryEnableListQueryExe categoryEnableListQueryExe;

    @GetMapping
    @Operation(summary = "启用分类")
    public R<List<AppProductCategorySimpleRes>> categories() {
        return R.ok(
            categoryEnableListQueryExe.execute().stream()
                .map(ProductCategoryResConverter::category)
                .toList());
    }
}
