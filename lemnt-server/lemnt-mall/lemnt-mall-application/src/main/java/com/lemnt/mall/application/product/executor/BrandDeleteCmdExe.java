package com.lemnt.mall.application.product.executor;

import com.lemnt.mall.domain.product.gateway.BrandGateway;
import com.lemnt.mall.domain.product.model.ProductCatalogValidation;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 品牌删除命令执行器。
 */
@Component
@RequiredArgsConstructor
public class BrandDeleteCmdExe {
    private final BrandGateway brandGateway;

    /**
     * 执行品牌删除命令。
     */
    public boolean execute(Long id) {
        ProductCatalogValidation.id(id);
        return brandGateway.deleteBrand(id);
    }
}
