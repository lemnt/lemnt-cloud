package com.lemnt.mall.application.catalog.executor;

import com.lemnt.mall.application.catalog.converter.CatalogDTOConverter;
import com.lemnt.mall.application.catalog.dto.CategoryDTO;
import com.lemnt.mall.domain.catalog.gateway.CategoryGateway;
import com.lemnt.mall.domain.catalog.model.CatalogRules;

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
        CatalogRules.id(id);
        return CatalogDTOConverter.category(categoryGateway.category(id));
    }
}
