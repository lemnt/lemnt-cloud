package com.lemnt.product.domain.product.gateway;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.product.domain.product.model.ProductBrand;
import com.lemnt.product.domain.product.model.ProductBrandFilter;

import java.util.List;

/**
 * ProductBrand 表领域存储接口。
 */
public interface ProductBrandGateway {
    /**
     * 品牌分页，列表和总数使用相同筛选。
     */
    PageResult<ProductBrand> page(ProductBrandFilter filter);

    /**
     * 查询品牌。
     */
    List<ProductBrand> brands();

    /**
     * 品牌。
     */
    ProductBrand brand(Long id);

    /**
     * 保存品牌。
     */
    ProductBrand saveBrand(ProductBrand value);

    /**
     * 删除品牌。
     */
    boolean deleteBrand(Long id);
}
