package com.lemnt.mall.adapter.web.product;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.mall.adapter.web.product.converter.ProductCatalogReqConverter;
import com.lemnt.mall.adapter.web.product.converter.ProductCatalogResConverter;
import com.lemnt.mall.adapter.web.product.req.MallCategoryCreateReq;
import com.lemnt.mall.adapter.web.product.req.MallCategoryUpdateReq;
import com.lemnt.mall.adapter.web.product.res.MallCategoryCreateRes;
import com.lemnt.mall.adapter.web.product.res.MallCategoryDetailRes;
import com.lemnt.mall.adapter.web.product.res.MallCategoryListRes;
import com.lemnt.mall.adapter.web.product.res.MallCategoryUpdateRes;
import com.lemnt.mall.application.product.executor.CategoryDeleteCmdExe;
import com.lemnt.mall.application.product.executor.CategoryDetailQueryExe;
import com.lemnt.mall.application.product.executor.CategoryListQueryExe;
import com.lemnt.mall.application.product.executor.CategorySaveCmdExe;

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
 * 分类接口。
 */
@Tag(name = "商品Category")
@RestController
@RequestMapping("/mall/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryListQueryExe categoryListQueryExe;
    private final CategoryDetailQueryExe categoryDetailQueryExe;
    private final CategorySaveCmdExe categorySaveCmdExe;
    private final CategoryDeleteCmdExe categoryDeleteCmdExe;

    @Operation(summary = "查询列表，超过1000条时拒绝截断")
    @GetMapping
    @RequiresPermissions("mall:category:list")
    public R<List<MallCategoryListRes>> list() {
        return R.ok(categoryListQueryExe.execute().stream().map(ProductCatalogResConverter::categoryToMallCategoryListRes).toList());
    }

    @Operation(summary = "查询详情")
    @GetMapping("/{id}")
    @RequiresPermissions("mall:category:query")
    public R<MallCategoryDetailRes> detail(@PathVariable Long id) {
        var value = categoryDetailQueryExe.execute(id);
        return value == null ? R.fail("记录不存在") : R.ok(ProductCatalogResConverter.categoryToMallCategoryDetailRes(value));
    }

    @Operation(summary = "创建")
    @PostMapping
    @RequiresPermissions("mall:category:add")
    public R<MallCategoryCreateRes> create(@Valid @RequestBody MallCategoryCreateReq req) {
        return R.ok(
                ProductCatalogResConverter.categoryToMallCategoryCreateRes(
                categorySaveCmdExe.execute(ProductCatalogReqConverter.category(null, req))));
    }

    @Operation(summary = "修改")
    @PutMapping("/{id}")
    @RequiresPermissions("mall:category:edit")
    public R<MallCategoryUpdateRes> update(@PathVariable Long id, @Valid @RequestBody MallCategoryUpdateReq req) {
        return R.ok(
                ProductCatalogResConverter.categoryToMallCategoryUpdateRes(categorySaveCmdExe.execute(ProductCatalogReqConverter.category(id,

                req))));
    }

    @Operation(summary = "删除未被引用的记录")
    @DeleteMapping("/{id}")
    @RequiresPermissions("mall:category:remove")
    public R<Boolean> delete(@PathVariable Long id) {
        return R.ok(categoryDeleteCmdExe.execute(id));
    }
}
