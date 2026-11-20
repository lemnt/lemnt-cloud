package com.lemnt.product.application.product.executor;

import com.lemnt.product.application.product.dto.ProductCategoryDTO;
import com.lemnt.product.domain.product.gateway.ProductCategoryGateway;
import com.lemnt.product.domain.product.model.ProductCatalogValidation;

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
        return execute(null);
    }

    /** 指定分类编号只返回启用记录，保持基础设施的稳定顺序。 */
    public List<ProductCategoryDTO> execute(List<Long> ids) {
        if (ids != null) {
            if (ids.size() > 100) throw new IllegalArgumentException("分类最多100个");
            ids.forEach(ProductCatalogValidation::id);
        }
        return categoryGateway.listEnableCategories(ids).stream()
                .map(
                        v ->
                                new ProductCategoryDTO(
                                        v.id(),
                                        v.parentId(),
                                        v.name(),
                                        v.picUrl(),
                                        v.sort(),
                                        v.status()))
                .toList();
    }
}
