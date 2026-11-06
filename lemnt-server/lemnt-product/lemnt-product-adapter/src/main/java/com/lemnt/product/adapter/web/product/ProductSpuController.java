package com.lemnt.product.adapter.web.product;

import com.alibaba.excel.EasyExcel;
import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.product.adapter.web.product.converter.ProductCatalogReqConverter;
import com.lemnt.product.adapter.web.product.converter.ProductCatalogResConverter;
import com.lemnt.product.adapter.web.product.converter.ProductManagementReqConverter;
import com.lemnt.product.adapter.web.product.converter.ProductManagementResConverter;
import com.lemnt.product.adapter.web.product.req.*;
import com.lemnt.product.adapter.web.product.res.*;
import com.lemnt.product.application.product.executor.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

import jakarta.validation.Valid;

import java.io.ByteArrayOutputStream;
import java.util.List;

/**
 * 商品SPU管理。
 */
@Tag(name = "商品管理")
@RestController
@RequestMapping("/spus")
@RequiredArgsConstructor
public class ProductSpuController {
    private final ProductSpuPageQueryExe spuPageQueryExe;
    private final ProductSpuDetailQueryExe spuDetailQueryExe;
    private final ProductSpuCreateCmdExe spuCreateCmdExe;
    private final ProductSpuUpdateCmdExe spuUpdateCmdExe;
    private final ProductSpuShelfCmdExe spuShelfCmdExe;
    private final ProductSpuExportQueryExe spuExportQueryExe;
    private final ProductSpuRecycleCmdExe spuRecycleCmdExe;
    private final ProductSpuRestoreCmdExe spuRestoreCmdExe;
    private final ProductSpuDeleteCmdExe spuDeleteCmdExe;
    private final ProductSpuCountQueryExe spuCountQueryExe;
    private final ProductSpuSimpleListQueryExe spuSimpleListQueryExe;
    private final ProductSpuBatchDetailQueryExe spuBatchDetailQueryExe;

    @Operation(summary = "分页查询SPU，SKU请查询详情")
    @GetMapping
    @RequiresPermissions("product:spu:list")
    public R<ProductSpuPageRes> page(@Valid @ModelAttribute ProductSpuPageReq req) {
        var result = spuPageQueryExe.execute(ProductCatalogReqConverter.page(req));
        return R.page(
            result.records().stream()
                .map(ProductCatalogResConverter::spuToProductSpuPageRes)
                .toList(),
            result.total());
    }

    /**
     * 有界上架商品选择。
     */
    @GetMapping("/simple")
    @Operation(summary = "上架商品选择查询")
    @RequiresPermissions("product:spu:query")
    public R<List<ProductSpuSimpleRes>> simple(@Valid @ModelAttribute ProductSpuSimpleReq req) {
        return R.ok(
            spuSimpleListQueryExe
                .execute(ProductManagementReqConverter.simple(req))
                .stream()
                .map(ProductManagementResConverter::simple)
                .toList());
    }

    /**
     * 批量查询真实详情。
     */
    @GetMapping("/batch")
    @Operation(summary = "批量商品详情")
    @RequiresPermissions("product:spu:query")
    public R<List<ProductSpuBatchRes>> batch(@Valid @ModelAttribute ProductSpuBatchReq req) {
        return R.ok(
            spuBatchDetailQueryExe.execute(req.ids()).stream()
                .map(ProductManagementResConverter::batch)
                .toList());
    }

    /**
     * 查询同筛选条件下各状态数量。
     */
    @GetMapping("/count")
    @Operation(summary = "商品状态统计")
    @RequiresPermissions("product:spu:list")
    public R<ProductSpuCountRes> count(@Valid @ModelAttribute ProductSpuCountReq req) {
        return R.ok(
            ProductManagementResConverter.count(
                spuCountQueryExe.execute(
                    ProductManagementReqConverter.counts(req))));
    }

    @Operation(summary = "查询SPU与全部SKU详情")
    @GetMapping("/{id}")
    @RequiresPermissions("product:spu:query")
    public R<ProductSpuDetailRes> detail(@PathVariable Long id) {
        var value = spuDetailQueryExe.execute(id);
        return value == null
            ? R.fail("商品不存在")
            : R.ok(ProductCatalogResConverter.spuToProductSpuDetailRes(value));
    }

    @Operation(summary = "原子创建SPU及SKU")
    @PostMapping
    @RequiresPermissions("product:spu:add")
    public R<ProductSpuCreateRes> create(@Valid @RequestBody ProductSpuCreateReq req) {
        return R.ok(
            ProductCatalogResConverter.spuToProductSpuCreateRes(
                spuCreateCmdExe.execute(ProductCatalogReqConverter.create(req))));
    }

    @Operation(summary = "修改SPU资料，不修改SKU库存")
    @PutMapping("/{id}")
    @RequiresPermissions("product:spu:edit")
    public R<Boolean> update(@PathVariable Long id, @Valid @RequestBody ProductSpuUpdateReq req) {
        return R.ok(spuUpdateCmdExe.execute(ProductCatalogReqConverter.update(id, req)));
    }

    @Operation(summary = "上下架SPU")
    @PutMapping("/{id}/shelf")
    @RequiresPermissions("product:spu:edit")
    public R<Boolean> shelf(@PathVariable Long id, @Valid @RequestBody ProductSpuShelfReq req) {
        return R.ok(spuShelfCmdExe.execute(ProductCatalogReqConverter.shelf(id, req)));
    }

    /**
     * 导出当前业务筛选下所有商品，具有独立导出权限。
     */
    @GetMapping("/export")
    @Operation(summary = "导出商品Excel")
    @RequiresPermissions("product:spu:export")
    public ResponseEntity<byte[]> export(@Valid @ModelAttribute ProductSpuExportReq req)
        throws Exception {
        var rows =
            spuExportQueryExe.execute(ProductManagementReqConverter.export(req)).stream()
                .map(ProductManagementResConverter::export)
                .toList();
        try (var output = new ByteArrayOutputStream()) {
            EasyExcel.write(output)
                .autoCloseStream(false)
                .head(ProductSpuExportRes.class)
                .sheet("商品")
                .doWrite(rows);
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=products.xlsx")
                .contentType(
                    MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(output.toByteArray());
        }
    }

    /**
     * 下架商品放入回收站。
     */
    @PostMapping("/{id}/recycle")
    @Operation(summary = "商品放入回收站")
    @RequiresPermissions("product:spu:edit")
    public R<Boolean> recycle(@PathVariable Long id) {
        return R.ok(spuRecycleCmdExe.execute(id));
    }

    /**
     * 恢复为下架，不能直接恢复为上架。
     */
    @PostMapping("/{id}/restore")
    @Operation(summary = "恢复回收站商品")
    @RequiresPermissions("product:spu:edit")
    public R<Boolean> restore(@PathVariable Long id) {
        return R.ok(spuRestoreCmdExe.execute(id));
    }

    /**
     * 删除仍保留历史编号。
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除回收站商品")
    @RequiresPermissions("product:spu:remove")
    public R<Boolean> delete(@PathVariable Long id) {
        return R.ok(spuDeleteCmdExe.execute(id));
    }
}
