package com.lemnt.mall.infrastructure.product.mapper;

import com.lemnt.mall.infrastructure.product.dataobject.ProductSkuSpecDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/** mall_product_sku_spec 表（SKU规格快照）数据访问接口。 */
@Mapper
public interface ProductSkuSpecMapper extends BaseMapper<ProductSkuSpecDO> {

    /**
     * 配置属性按商品SPU。
     */
    List<ProductSkuSpecDO> propertiesBySpu(Long id);

}
