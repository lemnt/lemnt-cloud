package com.lemnt.product.application.product.executor;

import com.lemnt.product.application.product.dto.StoreCategoryDTO;
import com.lemnt.product.domain.product.gateway.CategoryGateway;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * 门店目录查询执行器。
 */
@Component
@RequiredArgsConstructor
public class StoreCategoryListQueryExe {
    private final CategoryGateway categoryGateway;

    /**
     * 执行门店目录查询。
     */
    public List<StoreCategoryDTO> execute() {
        return categoryGateway.visibleCategories().stream()
            .map(v -> new StoreCategoryDTO(v.id(), v.parentId(), v.name(), v.picUrl()))
            .toList();
    }
}
