package com.lemnt.mall.application.product.executor;

import com.lemnt.mall.application.product.converter.ProductCatalogDTOConverter;
import com.lemnt.mall.application.product.dto.CategoryDTO;
import com.lemnt.mall.domain.product.gateway.CategoryGateway;
import com.lemnt.mall.domain.product.model.ProductCatalogValidation;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 分类详情查询执行器。
 */
@Component
@RequiredArgsConstructor
public class CategoryDetailQueryExe {
    private final CategoryGateway categoryGateway;

    /**
     * 执行分类详情查询。
     */
    public CategoryDTO execute(Long id) {
        ProductCatalogValidation.id(id);
        return ProductCatalogDTOConverter.category(categoryGateway.category(id));
    }
}
