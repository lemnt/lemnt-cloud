package com.lemnt.product.infrastructure.product.mapper;

import com.lemnt.product.infrastructure.product.dataobject.ProductFavoriteDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/** mall_product_favorite的基础数据访问。 */
@Mapper
public interface ProductFavoriteMapper extends BaseMapper<ProductFavoriteDO> {}
