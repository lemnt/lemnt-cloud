package com.lemnt.mall.application.catalog.service;

import com.lemnt.mall.domain.catalog.gateway.CatalogGateway;
import com.lemnt.mall.domain.catalog.model.CatalogRules;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CatalogValidationService {
    private final CatalogGateway gateway;

    public void enabledReferences(Long categoryId, Long brandId) {
        CatalogRules.id(categoryId);
        CatalogRules.id(brandId);
        var category = gateway.category(categoryId);
        var brand = gateway.brand(brandId);
        if (category == null
                || !"ENABLED".equals(category.status())
                || brand == null
                || !"ENABLED".equals(brand.status())) {
            throw new IllegalArgumentException("分类或品牌不存在或已停用");
        }
        if (category.parentId() != null) {
            var parent = gateway.category(category.parentId());
            if (parent == null || !"ENABLED".equals(parent.status())) {
                throw new IllegalArgumentException("父分类已停用");
            }
        }
    }
}
