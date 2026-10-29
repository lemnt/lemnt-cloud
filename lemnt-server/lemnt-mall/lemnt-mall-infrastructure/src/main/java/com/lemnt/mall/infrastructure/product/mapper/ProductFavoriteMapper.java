package com.lemnt.mall.infrastructure.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lemnt.mall.infrastructure.product.dataobject.ProductFavoriteDO;
import org.apache.ibatis.annotations.Mapper;

/** mall_product_favorite的基础数据访问。 */
@Mapper
public interface ProductFavoriteMapper extends BaseMapper<ProductFavoriteDO> {}
