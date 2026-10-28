package com.lemnt.mall.domain.product.gateway;

import com.lemnt.mall.domain.product.model.Category;

import java.util.List;

/** Category 表领域存储接口。 */
public interface CategoryGateway {
    /**
     * 查询分类。
     */
    List<Category> categories();

    /**
     * 分类。
     */
    Category category(Long id);

    /**
     * 保存分类。
     */
    Category saveCategory(Category value);

    /**
     * 删除分类。
     */
    boolean deleteCategory(Long id);

    /**
     * 查询分类。
     */
    List<Category> visibleCategories();
}
