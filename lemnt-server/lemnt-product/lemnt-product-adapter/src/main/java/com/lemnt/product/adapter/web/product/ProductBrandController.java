package com.lemnt.product.adapter.web.product;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.product.adapter.web.product.converter.ProductCatalogReqConverter;
import com.lemnt.product.adapter.web.product.converter.ProductCatalogResConverter;
import com.lemnt.product.adapter.web.product.converter.ProductManagementReqConverter;
import com.lemnt.product.adapter.web.product.converter.ProductManagementResConverter;
import com.lemnt.product.adapter.web.product.req.MallBrandCreateReq;
import com.lemnt.product.adapter.web.product.req.MallBrandUpdateReq;
import com.lemnt.product.adapter.web.product.req.ProductBrandPageReq;
import com.lemnt.product.adapter.web.product.req.ProductBrandSimpleReq;
import com.lemnt.product.adapter.web.product.res.*;
import com.lemnt.product.application.product.executor.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

import jakarta.validation.Valid;

import java.util.List;

/**
 * 商品品牌
 */
@Tag(name = "商品品牌")
@RestController
@RequestMapping("/brands")
@RequiredArgsConstructor
public class ProductBrandController {

    private final ProductBrandPageQueryExe brandPageQueryExe;
    private final ProductBrandSimpleListQueryExe brandSimpleListQueryExe;
    private final ProductBrandListQueryExe brandListQueryExe;
    private final ProductBrandDetailQueryExe brandDetailQueryExe;
    private final ProductBrandSaveCmdExe brandSaveCmdExe;
    private final ProductBrandDeleteCmdExe brandDeleteCmdExe;

    /**
     * 品牌管理分页。
     */
    @GetMapping("/page")
    @Operation(summary = "品牌分页")
    @RequiresPermissions("product:brand:list")
    public R<ProductBrandPageRes> page(@Valid @ModelAttribute ProductBrandPageReq req) {
        var value = brandPageQueryExe.execute(ProductManagementReqConverter.brands(req));
        return R.page(
            value.getRecords().stream().map(ProductManagementResConverter::brand).toList(),
            value.getTotal());
    }

    @Operation(summary = "查询列表，超过1000条时拒绝截断")
    @GetMapping
    @RequiresPermissions("product:brand:list")
    public R<List<MallBrandListRes>> list() {
        return R.ok(
            brandListQueryExe.execute().stream()
                .map(ProductCatalogResConverter::brandToMallBrandListRes)
                .toList());
    }

    /**
     * 查询启用品牌供选择。
     */
    @GetMapping("/simple")
    @Operation(summary = "启用品牌选择查询")
    @RequiresPermissions("product:brand:query")
    public R<List<ProductBrandSimpleRes>> simple(@Valid @ModelAttribute ProductBrandSimpleReq req) {
        return R.ok(
            brandSimpleListQueryExe
                .execute(ProductManagementReqConverter.brandSimple(req))
                .stream()
                .map(ProductManagementResConverter::brandSimple)
                .toList());
    }

    @Operation(summary = "查询详情")
    @GetMapping("/{id}")
    @RequiresPermissions("product:brand:query")
    public R<MallBrandDetailRes> detail(@PathVariable Long id) {
        var value = brandDetailQueryExe.execute(id);
        return value == null
            ? R.fail("记录不存在")
            : R.ok(ProductCatalogResConverter.brandToMallBrandDetailRes(value));
    }

    @Operation(summary = "创建")
    @PostMapping
    @RequiresPermissions("product:brand:add")
    public R<MallBrandCreateRes> create(@Valid @RequestBody MallBrandCreateReq req) {
        return R.ok(
            ProductCatalogResConverter.brandToMallBrandCreateRes(
                brandSaveCmdExe.execute(ProductCatalogReqConverter.brand(null, req))));
    }

    @Operation(summary = "修改")
    @PutMapping("/{id}")
    @RequiresPermissions("product:brand:edit")
    public R<MallBrandUpdateRes> update(
        @PathVariable Long id, @Valid @RequestBody MallBrandUpdateReq req) {
        return R.ok(
            ProductCatalogResConverter.brandToMallBrandUpdateRes(
                brandSaveCmdExe.execute(ProductCatalogReqConverter.brand(id, req))));
    }

    @Operation(summary = "删除未被引用的记录")
    @DeleteMapping("/{id}")
    @RequiresPermissions("product:brand:remove")
    public R<Boolean> delete(@PathVariable Long id) {
        return R.ok(brandDeleteCmdExe.execute(id));
    }
}
