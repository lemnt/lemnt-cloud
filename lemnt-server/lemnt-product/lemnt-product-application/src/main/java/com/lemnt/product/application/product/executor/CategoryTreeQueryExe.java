package com.lemnt.product.application.product.executor;

import com.lemnt.product.application.product.dto.CategoryTreeDTO;
import com.lemnt.product.domain.product.gateway.CategoryGateway;
import com.lemnt.product.domain.product.model.Category;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Objects;

/**
 * 分类树查询。
 */
@Component
@RequiredArgsConstructor
public class CategoryTreeQueryExe {
    private final CategoryGateway categoryGateway;

    /**
     * 按现有父子分类生成树，沿用列表排序及超量拒绝规则。
     */
    public List<CategoryTreeDTO> execute() {
        return children(categoryGateway.categories(), null);
    }

    private List<CategoryTreeDTO> children(List<Category> rows, Long parentId) {
        return rows.stream()
            .filter(value -> Objects.equals(value.parentId(), parentId))
            .map(
                value ->
                    new CategoryTreeDTO(
                        value.id(),
                        value.name(),
                        value.picUrl(),
                        value.sort(),
                        value.status(),
                        children(rows, value.id())))
            .toList();
    }
}
