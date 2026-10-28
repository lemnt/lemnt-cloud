package com.lemnt.mall.domain.product.gateway;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.mall.domain.product.model.Brand;
import com.lemnt.mall.domain.product.model.BrandFilter;

import java.util.List;

/** Brand 表领域存储接口。 */
public interface BrandGateway {
    /** 品牌分页，列表和总数使用相同筛选。 */
    PageResult<Brand> page(BrandFilter filter);

    /**
     * 查询品牌。
     */
    List<Brand> brands();

    /**
     * 品牌。
     */
    Brand brand(Long id);

    /**
     * 保存品牌。
     */
    Brand saveBrand(Brand value);

    /**
     * 删除品牌。
     */
    boolean deleteBrand(Long id);
}
