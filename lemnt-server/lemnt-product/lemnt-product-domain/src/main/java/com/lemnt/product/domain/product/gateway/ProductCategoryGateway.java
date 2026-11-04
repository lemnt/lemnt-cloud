package com.lemnt.product.domain.product.gateway;

import com.lemnt.product.domain.product.model.ProductCategory;

import java.util.List;

/**
 * ProductCategory 表领域存储接口。
 */
public interface ProductCategoryGateway {
    /**
     * 查询分类。
     */
    List<ProductCategory> categories();

    /**
     * 分类。
     */
    ProductCategory category(Long id);

    /**
     * 保存分类。
     */
    ProductCategory saveCategory(ProductCategory value);

    /**
     * 删除分类。
     */
    boolean deleteCategory(Long id);

    /**
     * 查询分类。
     */
    List<ProductCategory> visibleCategories();
}
