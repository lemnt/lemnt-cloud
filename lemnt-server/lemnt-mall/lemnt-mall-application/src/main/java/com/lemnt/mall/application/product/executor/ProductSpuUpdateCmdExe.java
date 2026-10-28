package com.lemnt.mall.application.product.executor;

import com.lemnt.mall.application.product.cmd.ProductSpuUpdateCmd;
import com.lemnt.mall.application.product.service.ProductCatalogService;
import com.lemnt.mall.application.product.service.ProductValidationService;
import com.lemnt.mall.domain.product.model.ProductCatalogValidation;
import com.lemnt.mall.domain.product.model.ProductSpu;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/**
 * 商品SPU更新命令执行器。
 */
@Component
@RequiredArgsConstructor
public class ProductSpuUpdateCmdExe {
    private final ProductCatalogService productCatalogService;
    private final ProductValidationService catalogValidationService;

    /**
     * 执行商品SPU更新命令。
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean execute(ProductSpuUpdateCmd cmd) {
        if (cmd == null
        ) {
            throw new IllegalArgumentException("商品参数不能为空");
        }
        ProductCatalogValidation.id(cmd.id());
        catalogValidationService.enabledReferences(cmd.categoryId(), cmd.brandId());
        return productCatalogService.updateSpu(
                new ProductSpu(
                cmd.id(),
                null,
                ProductCatalogValidation.text(cmd.name(), 200, "商品名称"),
                cmd.categoryId(),
                cmd.brandId(),
                ProductCatalogValidation.optional(cmd.description(), 65535),
                ProductCatalogValidation.optional(cmd.image(), 500),
                null,
                cmd.skus(), cmd.deliveryTypes(), cmd.deliveryTemplateId()));
    }
}
