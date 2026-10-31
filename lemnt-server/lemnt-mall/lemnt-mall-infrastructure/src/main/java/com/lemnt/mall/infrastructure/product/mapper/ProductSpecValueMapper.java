package com.lemnt.mall.infrastructure.product.mapper;

import com.lemnt.mall.infrastructure.product.dataobject.ProductSpecValueDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/** mall_product_spec_value表访问接口。 */
@Mapper
public interface ProductSpecValueMapper extends BaseMapper<ProductSpecValueDO> {
    /** 锁定字典记录。 */
    ProductSpecValueDO lock(Long id);
}
