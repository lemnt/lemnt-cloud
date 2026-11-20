package com.lemnt.product.application.product.executor;

import com.lemnt.product.application.product.dto.ProductCategoryTreeDTO;
import com.lemnt.product.domain.product.gateway.ProductCategoryGateway;
import com.lemnt.product.domain.product.model.ProductCategory;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Objects;

/**
 * 分类树查询。
 */
@Component
@RequiredArgsConstructor
public class ProductCategoryTreeQueryExe {
    private final ProductCategoryGateway categoryGateway;

    /**
     * 按现有父子分类生成树，沿用列表排序及超量拒绝规则。
     */
    public List<ProductCategoryTreeDTO> execute() {
        return children(categoryGateway.categories(), null);
    }

    private List<ProductCategoryTreeDTO> children(List<ProductCategory> rows, Long parentId) {
        return rows.stream()
            .filter(value -> Objects.equals(value.parentId(), parentId))
            .map(
                value ->
                    new ProductCategoryTreeDTO(
                        value.id(),
                        value.name(),
                        value.picUrl(),
                        value.sort(),
                        value.status(),
                        children(rows, value.id())))
            .toList();
    }
}
