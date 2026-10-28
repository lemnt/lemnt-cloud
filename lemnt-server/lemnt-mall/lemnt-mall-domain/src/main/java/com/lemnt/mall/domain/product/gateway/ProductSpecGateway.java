package com.lemnt.mall.domain.product.gateway;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.mall.domain.product.model.ProductSpec;
import com.lemnt.mall.domain.product.model.ProductSpecFilter;

/** mall_product_spec表领域存储接口。 */
public interface ProductSpecGateway {
    /** 查询详情。 */
    ProductSpec find(Long id);

    /** 锁定记录。 */
    ProductSpec lock(Long id);

    /** 保存规格字典记录。 */
    ProductSpec save(ProductSpec value);

    /** 删除未引用的记录。 */
    boolean delete(Long id);

    /** 分页查询。 */
    PageResult<ProductSpec> page(ProductSpecFilter filter);
}
