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
import java.util.List;

@Tag(name = "商品Brand")
@RestController
@RequestMapping("/mall/brands")
@RequiredArgsConstructor
public class BrandController {
    private final BrandListQueryExe list;
    private final BrandDetailQueryExe detail;
    private final BrandSaveCmdExe save;
    private final BrandDeleteCmdExe delete;

    @Operation(summary = "查询列表，超过1000条时拒绝截断")
    @GetMapping
    @RequiresPermissions("mall:brand:list")
    public R<List<BrandRes>> list() {
        return R.ok(list.execute().stream().map(CatalogResConverter::brand).toList());
    }

    @Operation(summary = "查询详情")
    @GetMapping("/{id}")
    @RequiresPermissions("mall:brand:query")
    public R<BrandRes> detail(@PathVariable Long id) {
        var value = detail.execute(id);
        return value == null ? R.fail("记录不存在") : R.ok(CatalogResConverter.brand(value));
    }

    @Operation(summary = "创建")
    @PostMapping
    @RequiresPermissions("mall:brand:add")
    public R<BrandRes> create(@Valid @RequestBody BrandSaveReq req) {
        return R.ok(CatalogResConverter.brand(save.execute(CatalogReqConverter.brand(null, req))));
    }

    @Operation(summary = "修改")
    @PutMapping("/{id}")
    @RequiresPermissions("mall:brand:edit")
    public R<BrandRes> update(@PathVariable Long id, @Valid @RequestBody BrandSaveReq req) {
        return R.ok(CatalogResConverter.brand(save.execute(CatalogReqConverter.brand(id, req))));
    }

    @Operation(summary = "删除未被引用的记录")
    @DeleteMapping("/{id}")
    @RequiresPermissions("mall:brand:remove")
    public R<Boolean> delete(@PathVariable Long id) {
        return R.ok(delete.execute(id));
    }
}
