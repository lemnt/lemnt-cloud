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
     * 查询分类。
     */
    List<ProductCategory> listEnableCategories();

    /**
     * 分类。
     */
    ProductCategory category(Long id);

    /** 查询指定启用分类；null表示全部，空集合返回空，按sort/id稳定升序。 */
    List<ProductCategory> listEnableCategories(List<Long> ids);

    /** 批量查询启用的直接子分类，不递归扩展，也不逐分类发起查询。 */
    List<ProductCategory> enabledChildren(List<Long> parentIds);

    /**
     * 保存分类。
     */
    ProductCategory saveCategory(ProductCategory value);

    /**
     * 删除分类。
     */
    boolean deleteCategory(Long id);
}
