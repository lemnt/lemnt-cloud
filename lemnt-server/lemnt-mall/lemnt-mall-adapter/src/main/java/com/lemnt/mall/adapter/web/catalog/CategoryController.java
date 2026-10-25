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

@Tag(name = "商品Category")
@RestController
@RequestMapping("/mall/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryListQueryExe list;
    private final CategoryDetailQueryExe detail;
    private final CategorySaveCmdExe save;
    private final CategoryDeleteCmdExe delete;

    @Operation(summary = "查询列表，超过1000条时拒绝截断")
    @GetMapping
    @RequiresPermissions("mall:category:list")
    public R<List<CategoryRes>> list() {
        return R.ok(list.execute().stream().map(CatalogResConverter::category).toList());
    }

    @Operation(summary = "查询详情")
    @GetMapping("/{id}")
    @RequiresPermissions("mall:category:query")
    public R<CategoryRes> detail(@PathVariable Long id) {
        var value = detail.execute(id);
        return value == null ? R.fail("记录不存在") : R.ok(CatalogResConverter.category(value));
    }

    @Operation(summary = "创建")
    @PostMapping
    @RequiresPermissions("mall:category:add")
    public R<CategoryRes> create(@Valid @RequestBody CategorySaveReq req) {
        return R.ok(
                CatalogResConverter.category(
                        save.execute(CatalogReqConverter.category(null, req))));
    }

    @Operation(summary = "修改")
    @PutMapping("/{id}")
    @RequiresPermissions("mall:category:edit")
    public R<CategoryRes> update(@PathVariable Long id, @Valid @RequestBody CategorySaveReq req) {
        return R.ok(
                CatalogResConverter.category(save.execute(CatalogReqConverter.category(id, req))));
    }

    @Operation(summary = "删除未被引用的记录")
    @DeleteMapping("/{id}")
    @RequiresPermissions("mall:category:remove")
    public R<Boolean> delete(@PathVariable Long id) {
        return R.ok(delete.execute(id));
    }
}
