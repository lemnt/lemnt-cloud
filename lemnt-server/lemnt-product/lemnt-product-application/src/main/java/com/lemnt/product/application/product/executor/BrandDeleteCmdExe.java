package com.lemnt.product.application.product.executor;

import com.lemnt.product.domain.product.gateway.BrandGateway;
import com.lemnt.product.domain.product.model.ProductCatalogValidation;

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
