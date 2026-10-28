package com.lemnt.mall.application.product.executor;

import com.lemnt.mall.application.product.cmd.ProductSpuShelfCmd;
import com.lemnt.mall.application.product.service.ProductCatalogService;
import com.lemnt.mall.application.product.service.ProductValidationService;
import com.lemnt.mall.domain.product.gateway.ProductSpuGateway;
import com.lemnt.mall.domain.product.model.ProductCatalogValidation;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/**
 * 商品商品SPU上架命令执行器。
 */
@Component
@RequiredArgsConstructor
public class ProductSpuShelfCmdExe {
    private final ProductCatalogService catalogService;
    private final ProductSpuGateway productSpuGateway;
    private final ProductValidationService catalogValidationService;

    /**
     * 执行商品商品SPU上架命令。
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean execute(ProductSpuShelfCmd cmd) {
        if (cmd == null) {
            throw new IllegalArgumentException("上架参数不能为空");
        }
        ProductCatalogValidation.id(cmd.id());
        var value = catalogService.spu(cmd.id());
        if (value == null) {
            return false;
        }
        if (!java.util.Set.of("ON_SHELF", "OFF_SHELF").contains(value.status())) {
            throw new IllegalStateException("回收站商品须先恢复");
        }
        if (cmd.onShelf()) {
            catalogValidationService.enabledReferences(value.categoryId(), value.brandId());
            if (value.skus().stream().noneMatch(sku -> "ON_SHELF".equals(sku.status()))) {
                throw new IllegalStateException("没有SKU不能上架");
            }
        }
        return productSpuGateway.shelf(cmd.id(), cmd.onShelf());
    }
}
