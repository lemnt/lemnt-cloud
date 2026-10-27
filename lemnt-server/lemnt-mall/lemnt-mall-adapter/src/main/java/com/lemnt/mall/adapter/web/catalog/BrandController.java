package com.lemnt.mall.adapter.web.catalog;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.mall.adapter.web.catalog.converter.CatalogReqConverter;
import com.lemnt.mall.adapter.web.catalog.converter.CatalogResConverter;
import com.lemnt.mall.adapter.web.catalog.req.MallBrandCreateReq;
import com.lemnt.mall.adapter.web.catalog.req.MallBrandUpdateReq;
import com.lemnt.mall.adapter.web.catalog.res.MallBrandCreateRes;
import com.lemnt.mall.adapter.web.catalog.res.MallBrandDetailRes;
import com.lemnt.mall.adapter.web.catalog.res.MallBrandListRes;
import com.lemnt.mall.adapter.web.catalog.res.MallBrandUpdateRes;
import com.lemnt.mall.application.catalog.executor.BrandDeleteCmdExe;
import com.lemnt.mall.application.catalog.executor.BrandDetailQueryExe;
import com.lemnt.mall.application.catalog.executor.BrandListQueryExe;
import com.lemnt.mall.application.catalog.executor.BrandSaveCmdExe;

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
 * 品牌接口。
 */
@Tag(name = "商品Brand")
@RestController
@RequestMapping("/mall/brands")
@RequiredArgsConstructor
public class BrandController {
    private final BrandListQueryExe brandListQueryExe;
    private final BrandDetailQueryExe brandDetailQueryExe;
    private final BrandSaveCmdExe brandSaveCmdExe;
    private final BrandDeleteCmdExe brandDeleteCmdExe;

    @Operation(summary = "查询列表，超过1000条时拒绝截断")
    @GetMapping
    @RequiresPermissions("mall:brand:list")
    public R<List<MallBrandListRes>> list() {
        return R.ok(brandListQueryExe.execute().stream().map(CatalogResConverter::brandToMallBrandListRes).toList());
    }

    @Operation(summary = "查询详情")
    @GetMapping("/{id}")
    @RequiresPermissions("mall:brand:query")
    public R<MallBrandDetailRes> detail(@PathVariable Long id) {
        var value = brandDetailQueryExe.execute(id);
        return value == null ? R.fail("记录不存在") : R.ok(CatalogResConverter.brandToMallBrandDetailRes(value));
    }

    @Operation(summary = "创建")
    @PostMapping
    @RequiresPermissions("mall:brand:add")
    public R<MallBrandCreateRes> create(@Valid @RequestBody MallBrandCreateReq req) {
        return R.ok(CatalogResConverter.brandToMallBrandCreateRes(brandSaveCmdExe.execute(CatalogReqConverter.brand(null,

                req))));
    }

    @Operation(summary = "修改")
    @PutMapping("/{id}")
    @RequiresPermissions("mall:brand:edit")
    public R<MallBrandUpdateRes> update(@PathVariable Long id, @Valid @RequestBody MallBrandUpdateReq req) {
        return R.ok(CatalogResConverter.brandToMallBrandUpdateRes(brandSaveCmdExe.execute(CatalogReqConverter.brand(id,
                req))));
    }

    @Operation(summary = "删除未被引用的记录")
    @DeleteMapping("/{id}")
    @RequiresPermissions("mall:brand:remove")
    public R<Boolean> delete(@PathVariable Long id) {
        return R.ok(brandDeleteCmdExe.execute(id));
    }
}
