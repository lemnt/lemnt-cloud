package com.lemnt.product.application.product.executor;

import com.lemnt.product.application.product.converter.ProductCatalogDTOConverter;
import com.lemnt.product.application.product.dto.BrandDTO;
import com.lemnt.product.domain.product.gateway.BrandGateway;
import com.lemnt.product.domain.product.model.ProductCatalogValidation;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 品牌详情查询执行器。
 */
@Component
@RequiredArgsConstructor
public class BrandDetailQueryExe {
    private final BrandGateway brandGateway;

    /**
     * 执行品牌详情查询。
     */
    public BrandDTO execute(Long id) {
        ProductCatalogValidation.id(id);
        return ProductCatalogDTOConverter.brand(brandGateway.brand(id));
    }
}
