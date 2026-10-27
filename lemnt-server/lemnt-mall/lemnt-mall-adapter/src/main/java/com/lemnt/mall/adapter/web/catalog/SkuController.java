package com.lemnt.mall.adapter.web.catalog;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.mall.adapter.web.catalog.converter.CatalogReqConverter;
import com.lemnt.mall.adapter.web.catalog.req.SkuUpdateReq;
import com.lemnt.mall.application.catalog.executor.SkuUpdateCmdExe;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import jakarta.validation.Valid;

/** 商品SKU接口。 */
@Tag(name = "商品SKU")
@RestController
@RequestMapping("/mall/skus")
@RequiredArgsConstructor
public class SkuController {
    private final SkuUpdateCmdExe skuUpdateCmdExe;

    @Operation(summary = "修改SKU资料，不修改编码或规格组合")
    @PutMapping("/{id}")
    @RequiresPermissions("mall:sku:edit")
    public R<Boolean> update(@PathVariable Long id, @Valid @RequestBody SkuUpdateReq req) {
        return R.ok(skuUpdateCmdExe.execute(CatalogReqConverter.sku(id, req)));
    }
}
