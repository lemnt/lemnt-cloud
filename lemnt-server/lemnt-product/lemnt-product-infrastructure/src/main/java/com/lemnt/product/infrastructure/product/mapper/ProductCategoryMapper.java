package com.lemnt.product.infrastructure.product.mapper;

import com.lemnt.product.infrastructure.product.dataobject.ProductCategoryDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * ProductCategory 表数据访问接口。
 */
@Mapper
public interface ProductCategoryMapper extends BaseMapper<ProductCategoryDO> {
}
