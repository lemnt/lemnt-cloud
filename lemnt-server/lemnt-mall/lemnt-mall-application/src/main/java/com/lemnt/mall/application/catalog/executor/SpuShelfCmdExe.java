package com.lemnt.mall.application.catalog.executor;

import com.lemnt.mall.application.catalog.cmd.SpuShelfCmd;
import com.lemnt.mall.application.catalog.service.CatalogService;
import com.lemnt.mall.application.catalog.service.CatalogValidationService;
import com.lemnt.mall.domain.catalog.gateway.SpuGateway;
import com.lemnt.mall.domain.catalog.model.CatalogRules;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/**
 * 商品商品SPU上架命令执行器。
 */
@Component
@RequiredArgsConstructor
public class SpuShelfCmdExe {
    private final CatalogService catalogService;
    private final SpuGateway spuGateway;
    private final CatalogValidationService catalogValidationService;

    /**
     * 执行商品商品SPU上架命令。
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean execute(SpuShelfCmd cmd) {
        if (cmd == null) {
            throw new IllegalArgumentException("上架参数不能为空");
        }
        CatalogRules.id(cmd.id());
        var value = catalogService.spu(cmd.id());
        if (value == null) {
            return false;
        }
        if (cmd.onShelf()) {
            catalogValidationService.enabledReferences(value.categoryId(), value.brandId());
            if (value.skus().isEmpty()) {
                throw new IllegalStateException("没有SKU不能上架");
            }
        }
        return spuGateway.shelf(cmd.id(), cmd.onShelf());
    }
}
