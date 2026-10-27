package com.lemnt.mall.adapter.web.catalog;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.mall.adapter.web.catalog.converter.CatalogReqConverter;
import com.lemnt.mall.adapter.web.catalog.converter.CatalogResConverter;
import com.lemnt.mall.adapter.web.catalog.req.SpuCreateReq;
import com.lemnt.mall.adapter.web.catalog.req.SpuPageReq;
import com.lemnt.mall.adapter.web.catalog.req.SpuShelfReq;
import com.lemnt.mall.adapter.web.catalog.req.SpuUpdateReq;
import com.lemnt.mall.adapter.web.catalog.res.MallSpuCreateRes;
import com.lemnt.mall.adapter.web.catalog.res.MallSpuDetailRes;
import com.lemnt.mall.adapter.web.catalog.res.MallSpuPageRes;
import com.lemnt.mall.application.catalog.executor.SpuCreateCmdExe;
import com.lemnt.mall.application.catalog.executor.SpuDetailQueryExe;
import com.lemnt.mall.application.catalog.executor.SpuPageQueryExe;
import com.lemnt.mall.application.catalog.executor.SpuShelfCmdExe;
import com.lemnt.mall.application.catalog.executor.SpuUpdateCmdExe;

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
public class SpuController {
    private final SpuPageQueryExe spuPageQueryExe;
    private final SpuDetailQueryExe spuDetailQueryExe;
    private final SpuCreateCmdExe spuCreateCmdExe;
    private final SpuUpdateCmdExe spuUpdateCmdExe;
    private final SpuShelfCmdExe spuShelfCmdExe;

    @Operation(summary = "分页查询SPU，SKU请查询详情")
    @GetMapping
    @RequiresPermissions("mall:spu:list")
    public R<MallSpuPageRes> page(@Valid @ModelAttribute SpuPageReq req) {
        var result = spuPageQueryExe.execute(CatalogReqConverter.page(req));
        return R.page(
                result.records().stream().map(CatalogResConverter::spuToMallSpuPageRes).toList(), result.total());
    }

    @Operation(summary = "查询SPU与全部SKU详情")
    @GetMapping("/{id}")
    @RequiresPermissions("mall:spu:query")
    public R<MallSpuDetailRes> detail(@PathVariable Long id) {
        var value = spuDetailQueryExe.execute(id);
        return value == null ? R.fail("商品不存在") : R.ok(CatalogResConverter.spuToMallSpuDetailRes(value));
    }

    @Operation(summary = "原子创建SPU及SKU")
    @PostMapping
    @RequiresPermissions("mall:spu:add")
    public R<MallSpuCreateRes> create(@Valid @RequestBody SpuCreateReq req) {
        return R.ok(CatalogResConverter.spuToMallSpuCreateRes(spuCreateCmdExe.execute(CatalogReqConverter.create(req))));
    }

    @Operation(summary = "修改SPU资料，不修改SKU库存")
    @PutMapping("/{id}")
    @RequiresPermissions("mall:spu:edit")
    public R<Boolean> update(@PathVariable Long id, @Valid @RequestBody SpuUpdateReq req) {
        return R.ok(spuUpdateCmdExe.execute(CatalogReqConverter.update(id, req)));
    }

    @Operation(summary = "上下架SPU")
    @PutMapping("/{id}/shelf")
    @RequiresPermissions("mall:spu:edit")
    public R<Boolean> shelf(@PathVariable Long id, @Valid @RequestBody SpuShelfReq req) {
        return R.ok(spuShelfCmdExe.execute(CatalogReqConverter.shelf(id, req)));
    }
}
