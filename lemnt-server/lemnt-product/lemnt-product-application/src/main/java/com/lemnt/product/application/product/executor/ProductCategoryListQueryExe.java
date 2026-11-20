package com.lemnt.product.application.product.executor;

import com.lemnt.product.application.product.converter.ProductCatalogDTOConverter;
import com.lemnt.product.application.product.dto.ProductCategoryDTO;
import com.lemnt.product.domain.product.gateway.ProductCategoryGateway;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * 分类列表查询执行器。
 */
@Component
@RequiredArgsConstructor
public class ProductCategoryListQueryExe {
    private final ProductCategoryGateway categoryGateway;

    /**
     * 执行分类列表查询。
     */
    public List<ProductCategoryDTO> execute() {
        return categoryGateway.categories().stream()
            .map(ProductCatalogDTOConverter::category)
            .toList();
    }
}
