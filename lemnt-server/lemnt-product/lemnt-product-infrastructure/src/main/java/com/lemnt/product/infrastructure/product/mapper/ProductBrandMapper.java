package com.lemnt.product.infrastructure.product.mapper;

import com.lemnt.product.infrastructure.product.dataobject.ProductBrandDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * ProductBrand 表数据访问接口。
 */
@Mapper
public interface ProductBrandMapper extends BaseMapper<ProductBrandDO> {
}
