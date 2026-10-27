package com.lemnt.mall.adapter.web.catalog;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.mall.adapter.web.catalog.converter.CatalogReqConverter;
import com.lemnt.mall.adapter.web.catalog.converter.CatalogResConverter;
import com.lemnt.mall.adapter.web.catalog.req.StockAdjustReq;
import com.lemnt.mall.adapter.web.catalog.res.StockAdjustmentRes;
import com.lemnt.mall.application.catalog.executor.StockAdjustCmdExe;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import jakarta.validation.Valid;

/** 商品库存调整接口。 */
@Tag(name = "商品库存调整")
@RestController
@RequestMapping("/mall/skus")
@RequiredArgsConstructor
public class StockAdjustmentController {
    private final StockAdjustCmdExe stockAdjustCmdExe;

    @Operation(summary = "幂等增减可用库存")
    @PostMapping("/{id}/stock")
    @RequiresPermissions("mall:sku:stock")
    public R<StockAdjustmentRes> stock(
            @PathVariable Long id, @Valid @RequestBody StockAdjustReq req) {
        return R.ok(
                CatalogResConverter.stockAdjustment(
                stockAdjustCmdExe.execute(CatalogReqConverter.stock(id, req))));
    }
}
