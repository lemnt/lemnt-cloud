package com.lemnt.mall.infrastructure.product.mapper;

import com.lemnt.mall.infrastructure.product.dataobject.ProductSpecDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/** mall_product_spec表访问接口。 */
@Mapper
public interface ProductSpecMapper extends BaseMapper<ProductSpecDO> {
    /** 锁定字典记录。 */
    ProductSpecDO lock(Long id);
}
