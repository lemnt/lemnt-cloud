package com.lemnt.mall.adapter.web.product;

import com.alibaba.excel.EasyExcel;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.mall.adapter.web.product.req.ProductSpuExportReq;
import com.lemnt.mall.adapter.web.product.res.ProductSpuExportRes;
import com.lemnt.mall.application.product.executor.ProductSpuExportQueryExe;
import com.lemnt.mall.adapter.web.product.converter.ProductManagementReqConverter;
import com.lemnt.mall.adapter.web.product.converter.ProductManagementResConverter;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;

/** 商品文件导出接口，使用项目已管理的Excel库进行同步下载。 */
@Tag(name = "商品导出")
@RestController
@RequestMapping("/mall/spus")
@RequiredArgsConstructor
public class ProductSpuExportController {
    private final ProductSpuExportQueryExe export;

    /** 导出当前业务筛选下所有商品，具有独立导出权限。 */
    @GetMapping("/export")
    @Operation(summary = "导出商品Excel")
    @RequiresPermissions("mall:spu:export")
    public ResponseEntity<byte[]> export(@Valid @ModelAttribute ProductSpuExportReq req) throws Exception {
        var rows = export.execute(ProductManagementReqConverter.export(req)).stream()
                .map(ProductManagementResConverter::export).toList();
        try (var output = new ByteArrayOutputStream()) {
            EasyExcel.write(output).autoCloseStream(false)
                    .head(ProductSpuExportRes.class).sheet("商品").doWrite(rows);
            return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=products.xlsx")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(output.toByteArray());
        }
    }
}
