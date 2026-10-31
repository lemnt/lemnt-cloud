package com.lemnt.mall.infrastructure.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lemnt.mall.infrastructure.product.dataobject.ProductBrowseHistoryDO;
import org.apache.ibatis.annotations.Mapper;

/** mall_product_browse_history的基础数据访问。 */
@Mapper
public interface ProductBrowseHistoryMapper extends BaseMapper<ProductBrowseHistoryDO> {}
