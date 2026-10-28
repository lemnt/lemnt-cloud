package com.lemnt.mall.infrastructure.product.mapper;

import com.lemnt.mall.domain.product.model.StoreFilter;
import com.lemnt.mall.infrastructure.product.dataobject.ProductSpuDO;
import com.lemnt.mall.infrastructure.product.dataobject.StoreProductDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/** Spu 表数据访问接口。 */
@Mapper
public interface ProductSpuMapper extends BaseMapper<ProductSpuDO> {

    /**
     * 锁定商品SPU。
     */
    ProductSpuDO lockSpu(Long id);

    /**
     * 查询商品。
     */
    List<StoreProductDO> products(StoreFilter filter);

    /**
     * 数量。
     */
    long storeCatalogCount(StoreFilter filter);

    /**
     * 可见。
     */
    Long visible(Long id);

}
