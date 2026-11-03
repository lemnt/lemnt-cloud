package com.lemnt.product.application.product.executor;

import com.lemnt.product.domain.product.gateway.CategoryGateway;
import com.lemnt.product.domain.product.model.ProductCatalogValidation;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 分类删除命令执行器。
 */
@Component
@RequiredArgsConstructor
public class CategoryDeleteCmdExe {
    private final CategoryGateway categoryGateway;

    /**
     * 执行分类删除命令。
     */
    public boolean execute(Long id) {
        ProductCatalogValidation.id(id);
        return categoryGateway.deleteCategory(id);
    }
}
