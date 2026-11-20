package com.lemnt.product.application.product.executor;

import com.lemnt.product.application.product.converter.ProductCatalogDTOConverter;
import com.lemnt.product.application.product.dto.ProductBrandDTO;
import com.lemnt.product.domain.product.gateway.ProductBrandGateway;
import com.lemnt.product.domain.product.model.ProductCatalogValidation;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 品牌详情查询执行器。
 */
@Component
@RequiredArgsConstructor
public class ProductBrandDetailQueryExe {
    private final ProductBrandGateway brandGateway;

    /**
     * 执行品牌详情查询。
     */
    public ProductBrandDTO execute(Long id) {
        ProductCatalogValidation.id(id);
        return ProductCatalogDTOConverter.brand(brandGateway.brand(id));
    }
}
