package com.lemnt.mall.domain.catalog.gateway;

import com.lemnt.mall.domain.catalog.model.Brand;

import java.util.List;

/** Brand 表领域存储接口。 */
public interface BrandGateway {
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
