package com.lemnt.product.application.product.executor;

import com.lemnt.product.application.product.converter.ProductCatalogDTOConverter;
import com.lemnt.product.application.product.dto.ProductCategoryDTO;
import com.lemnt.product.domain.product.gateway.ProductCategoryGateway;
import com.lemnt.product.domain.product.model.ProductCatalogValidation;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 分类详情查询执行器。
 */
@Component
@RequiredArgsConstructor
public class ProductCategoryDetailQueryExe {
    private final ProductCategoryGateway categoryGateway;

    /**
     * 执行分类详情查询。
     */
    public ProductCategoryDTO execute(Long id) {
        ProductCatalogValidation.id(id);
        return ProductCatalogDTOConverter.category(categoryGateway.category(id));
    }
}
