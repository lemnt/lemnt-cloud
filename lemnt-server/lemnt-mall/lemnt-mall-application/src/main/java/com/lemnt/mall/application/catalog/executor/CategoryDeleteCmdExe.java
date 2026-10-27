package com.lemnt.mall.application.catalog.executor;

import com.lemnt.mall.domain.catalog.gateway.CategoryGateway;
import com.lemnt.mall.domain.catalog.model.CatalogRules;

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
        CatalogRules.id(id);
        return categoryGateway.deleteCategory(id);
    }
}
