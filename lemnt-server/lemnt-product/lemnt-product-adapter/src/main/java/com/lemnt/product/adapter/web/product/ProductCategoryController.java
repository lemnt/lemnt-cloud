package com.lemnt.product.adapter.web.product;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.product.adapter.web.product.converter.ProductCatalogReqConverter;
import com.lemnt.product.adapter.web.product.converter.ProductCatalogResConverter;
import com.lemnt.product.adapter.web.product.converter.ProductManagementResConverter;
import com.lemnt.product.adapter.web.product.req.MallCategoryCreateReq;
import com.lemnt.product.adapter.web.product.req.MallCategoryUpdateReq;
import com.lemnt.product.adapter.web.product.res.*;
import com.lemnt.product.application.product.executor.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import jakarta.validation.Valid;

import java.util.List;

/**
 * 商品分类
 */
@Tag(name = "商品分类")
@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class ProductCategoryController {
    private final ProductCategoryListQueryExe categoryListQueryExe;
    private final ProductCategoryTreeQueryExe categoryTreeQueryExe;
    private final ProductCategoryDetailQueryExe categoryDetailQueryExe;
    private final ProductCategorySaveCmdExe categorySaveCmdExe;
    private final ProductCategoryDeleteCmdExe categoryDeleteCmdExe;

    @Operation(summary = "查询列表，超过1000条时拒绝截断")
    @GetMapping
    @RequiresPermissions("product:category:list")
    public R<List<MallCategoryListRes>> list() {
        return R.ok(
            categoryListQueryExe.execute().stream()
                .map(ProductCatalogResConverter::categoryToMallCategoryListRes)
                .toList());
    }

    /**
     * 返回真实父子分类树。
     */
    @GetMapping("/tree")
    @Operation(summary = "分类树")
    @RequiresPermissions("product:category:list")
    public R<List<ProductCategoryTreeRes>> tree() {
        return R.ok(
            categoryTreeQueryExe.execute().stream()
                .map(ProductManagementResConverter::tree)
                .toList());
    }

    @Operation(summary = "查询详情")
    @GetMapping("/{id}")
    @RequiresPermissions("product:category:query")
    public R<MallCategoryDetailRes> detail(@PathVariable Long id) {
        var value = categoryDetailQueryExe.execute(id);
        return value == null
            ? R.fail("记录不存在")
            : R.ok(ProductCatalogResConverter.categoryToMallCategoryDetailRes(value));
    }

    @Operation(summary = "创建")
    @PostMapping
    @RequiresPermissions("product:category:add")
    public R<MallCategoryCreateRes> create(@Valid @RequestBody MallCategoryCreateReq req) {
        return R.ok(
            ProductCatalogResConverter.categoryToMallCategoryCreateRes(
                categorySaveCmdExe.execute(
                    ProductCatalogReqConverter.category(null, req))));
    }

    @Operation(summary = "修改")
    @PutMapping("/{id}")
    @RequiresPermissions("product:category:edit")
    public R<MallCategoryUpdateRes> update(
        @PathVariable Long id, @Valid @RequestBody MallCategoryUpdateReq req) {
        return R.ok(
            ProductCatalogResConverter.categoryToMallCategoryUpdateRes(
                categorySaveCmdExe.execute(ProductCatalogReqConverter.category(id, req))));
    }

    @Operation(summary = "删除未被引用的记录")
    @DeleteMapping("/{id}")
    @RequiresPermissions("product:category:remove")
    public R<Boolean> delete(@PathVariable Long id) {
        return R.ok(categoryDeleteCmdExe.execute(id));
    }

}
