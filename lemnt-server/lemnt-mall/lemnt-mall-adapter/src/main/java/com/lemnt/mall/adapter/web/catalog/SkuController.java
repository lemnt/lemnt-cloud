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

@Tag(name = "SKU及库存管理")
@RestController
@RequestMapping("/mall/skus")
@RequiredArgsConstructor
public class SkuController {
    private final SkuUpdateCmdExe update;
    private final StockAdjustCmdExe stock;

    @Operation(summary = "修改SKU资料，不修改编码或规格组合")
    @PutMapping("/{id}")
    @RequiresPermissions("mall:sku:edit")
    public R<Boolean> update(@PathVariable Long id, @Valid @RequestBody SkuUpdateReq req) {
        return R.ok(update.execute(CatalogReqConverter.sku(id, req)));
    }

    @Operation(summary = "幂等增减可用库存")
    @PostMapping("/{id}/stock")
    @RequiresPermissions("mall:sku:stock")
    public R<StockAdjustmentRes> stock(
            @PathVariable Long id, @Valid @RequestBody StockAdjustReq req) {
        return R.ok(
                CatalogResConverter.stockAdjustment(
                        stock.execute(CatalogReqConverter.stock(id, req))));
    }
}
