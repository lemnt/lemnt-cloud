package com.lemnt.mall.application.product.executor;

import com.lemnt.mall.application.product.converter.ProductCatalogDTOConverter;
import com.lemnt.mall.application.product.dto.CategoryDTO;
import com.lemnt.mall.domain.product.gateway.CategoryGateway;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * 分类列表查询执行器。
 */
@Component
@RequiredArgsConstructor
public class CategoryListQueryExe {
    private final CategoryGateway categoryGateway;

    /**
     * 执行分类列表查询。
     */
    public List<CategoryDTO> execute() {
        return categoryGateway.categories().stream().map(ProductCatalogDTOConverter::category).toList();
    }
}
