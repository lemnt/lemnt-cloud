package com.lemnt.product.application.product.executor;

import com.lemnt.product.application.product.dto.ProductCategoryDTO;
import com.lemnt.product.application.product.dto.ProductCategorySimpleDTO;
import com.lemnt.product.domain.product.gateway.ProductCategoryGateway;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * 启用分类查询执行器。
 */
@Component
@RequiredArgsConstructor
public class ProductCategoryEnableListQueryExe {
    private final ProductCategoryGateway categoryGateway;

    /**
     * 执行门店目录查询。
     */
    public List<ProductCategoryDTO> execute() {
        return categoryGateway.listEnableCategories().stream()
            .map(v -> new ProductCategoryDTO(v.id(), v.parentId(), v.name(), v.picUrl(), v.sort(), v.status()))
            .toList();
    }
}
