package com.lemnt.mall.adapter.web.catalog;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.mall.adapter.web.catalog.converter.*;
import com.lemnt.mall.adapter.web.catalog.req.*;
import com.lemnt.mall.adapter.web.catalog.res.*;
import com.lemnt.mall.application.catalog.executor.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

@Tag(name = "商品SPU管理")
@RestController
@RequestMapping("/mall/spus")
@RequiredArgsConstructor
public class SpuController {
    private final SpuPageQueryExe page;
    private final SpuDetailQueryExe detail;
    private final SpuCreateCmdExe create;
    private final SpuUpdateCmdExe update;
    private final SpuShelfCmdExe shelf;

    @Operation(summary = "分页查询SPU，SKU请查询详情")
    @GetMapping
    @RequiresPermissions("mall:spu:list")
    public R<SpuRes> page(@Valid @ModelAttribute SpuPageReq req) {
        var result = page.execute(CatalogReqConverter.page(req));
        return R.page(
                result.records().stream().map(CatalogResConverter::spu).toList(), result.total());
    }

    @Operation(summary = "查询SPU与全部SKU详情")
    @GetMapping("/{id}")
    @RequiresPermissions("mall:spu:query")
    public R<SpuRes> detail(@PathVariable Long id) {
        var value = detail.execute(id);
        return value == null ? R.fail("商品不存在") : R.ok(CatalogResConverter.spu(value));
    }

    @Operation(summary = "原子创建SPU及SKU")
    @PostMapping
    @RequiresPermissions("mall:spu:add")
    public R<SpuRes> create(@Valid @RequestBody SpuCreateReq req) {
        return R.ok(CatalogResConverter.spu(create.execute(CatalogReqConverter.create(req))));
    }

    @Operation(summary = "修改SPU资料，不修改SKU库存")
    @PutMapping("/{id}")
    @RequiresPermissions("mall:spu:edit")
    public R<Boolean> update(@PathVariable Long id, @Valid @RequestBody SpuUpdateReq req) {
        return R.ok(update.execute(CatalogReqConverter.update(id, req)));
    }

    @Operation(summary = "上下架SPU")
    @PutMapping("/{id}/shelf")
    @RequiresPermissions("mall:spu:edit")
    public R<Boolean> shelf(@PathVariable Long id, @Valid @RequestBody SpuShelfReq req) {
        return R.ok(shelf.execute(CatalogReqConverter.shelf(id, req)));
    }
}
