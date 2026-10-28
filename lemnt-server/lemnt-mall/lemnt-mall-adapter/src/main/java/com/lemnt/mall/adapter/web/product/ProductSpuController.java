package com.lemnt.mall.adapter.web.product;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.mall.adapter.web.product.converter.ProductCatalogReqConverter;
import com.lemnt.mall.adapter.web.product.converter.ProductCatalogResConverter;
import com.lemnt.mall.adapter.web.product.req.ProductSpuCreateReq;
import com.lemnt.mall.adapter.web.product.req.ProductSpuPageReq;
import com.lemnt.mall.adapter.web.product.req.ProductSpuShelfReq;
import com.lemnt.mall.adapter.web.product.req.ProductSpuUpdateReq;
import com.lemnt.mall.adapter.web.product.res.ProductSpuCreateRes;
import com.lemnt.mall.adapter.web.product.res.ProductSpuDetailRes;
import com.lemnt.mall.adapter.web.product.res.ProductSpuPageRes;
import com.lemnt.mall.application.product.executor.ProductSpuCreateCmdExe;
import com.lemnt.mall.application.product.executor.ProductSpuDetailQueryExe;
import com.lemnt.mall.application.product.executor.ProductSpuPageQueryExe;
import com.lemnt.mall.application.product.executor.ProductSpuShelfCmdExe;
import com.lemnt.mall.application.product.executor.ProductSpuUpdateCmdExe;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
 * 商品SPU管理。
 */
@Tag(name = "商品SPU管理")
@RestController
@RequestMapping("/mall/spus")
@RequiredArgsConstructor
public class ProductSpuController {
    private final ProductSpuPageQueryExe productSpuPageQueryExe;
    private final ProductSpuDetailQueryExe productSpuDetailQueryExe;
    private final ProductSpuCreateCmdExe productSpuCreateCmdExe;
    private final ProductSpuUpdateCmdExe productSpuUpdateCmdExe;
    private final ProductSpuShelfCmdExe productSpuShelfCmdExe;

    @Operation(summary = "分页查询SPU，SKU请查询详情")
    @GetMapping
    @RequiresPermissions("mall:spu:list")
    public R<ProductSpuPageRes> page(@Valid @ModelAttribute ProductSpuPageReq req) {
        var result = productSpuPageQueryExe.execute(ProductCatalogReqConverter.page(req));
        return R.page(
                result.records().stream().map(ProductCatalogResConverter::spuToProductSpuPageRes).toList(), result.total());
    }

    @Operation(summary = "查询SPU与全部SKU详情")
    @GetMapping("/{id}")
    @RequiresPermissions("mall:spu:query")
    public R<ProductSpuDetailRes> detail(@PathVariable Long id) {
        var value = productSpuDetailQueryExe.execute(id);
        return value == null ? R.fail("商品不存在") : R.ok(ProductCatalogResConverter.spuToProductSpuDetailRes(value));
    }

    @Operation(summary = "原子创建SPU及SKU")
    @PostMapping
    @RequiresPermissions("mall:spu:add")
    public R<ProductSpuCreateRes> create(@Valid @RequestBody ProductSpuCreateReq req) {
        return R.ok(ProductCatalogResConverter.spuToProductSpuCreateRes(productSpuCreateCmdExe.execute(ProductCatalogReqConverter.create(req))));
    }

    @Operation(summary = "修改SPU资料，不修改SKU库存")
    @PutMapping("/{id}")
    @RequiresPermissions("mall:spu:edit")
    public R<Boolean> update(@PathVariable Long id, @Valid @RequestBody ProductSpuUpdateReq req) {
        return R.ok(productSpuUpdateCmdExe.execute(ProductCatalogReqConverter.update(id, req)));
    }

    @Operation(summary = "上下架SPU")
    @PutMapping("/{id}/shelf")
    @RequiresPermissions("mall:spu:edit")
    public R<Boolean> shelf(@PathVariable Long id, @Valid @RequestBody ProductSpuShelfReq req) {
        return R.ok(productSpuShelfCmdExe.execute(ProductCatalogReqConverter.shelf(id, req)));
    }
}
