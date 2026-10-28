package com.lemnt.mall.domain.product.gateway;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.mall.domain.product.model.ProductSpecFilter;
import com.lemnt.mall.domain.product.model.ProductSpecValue;

/** mall_product_spec_value表领域存储接口。 */
public interface ProductSpecValueGateway {
    /** 查询详情。 */
    ProductSpecValue find(Long id);

    /** 锁定记录。 */
    ProductSpecValue lock(Long id);

    /** 保存规格字典记录。 */
    ProductSpecValue save(ProductSpecValue value);

    /** 删除未引用的记录。 */
    boolean delete(Long id);

    /** 分页查询。 */
    PageResult<ProductSpecValue> page(ProductSpecFilter filter);
}
