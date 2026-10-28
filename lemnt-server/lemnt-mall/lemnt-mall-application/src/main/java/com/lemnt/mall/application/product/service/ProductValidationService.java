package com.lemnt.mall.application.product.service;

import com.lemnt.mall.domain.product.gateway.BrandGateway;
import com.lemnt.mall.domain.product.gateway.CategoryGateway;
import com.lemnt.mall.domain.product.model.ProductCatalogValidation;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 目录校验服务。
 */
@Component
@RequiredArgsConstructor
public class ProductValidationService {
    private final BrandGateway brandGateway;
    private final CategoryGateway categoryGateway;

    /**
     * 已启用引用。
     */
    public void enabledReferences(Long categoryId, Long brandId) {
        ProductCatalogValidation.id(categoryId);
        ProductCatalogValidation.id(brandId);
        var category = categoryGateway.category(categoryId);
        var brand = brandGateway.brand(brandId);
        if (category == null
                || !"ENABLED".equals(category.status())
                || brand == null
                || !"ENABLED".equals(brand.status())) {
            throw new IllegalArgumentException("分类或品牌不存在或已停用");
        }
        if (category.parentId() != null) {
            var parent = categoryGateway.category(category.parentId());
            if (parent == null || !"ENABLED".equals(parent.status())) {
                throw new IllegalArgumentException("父分类已停用");
            }
        }
    }
}
