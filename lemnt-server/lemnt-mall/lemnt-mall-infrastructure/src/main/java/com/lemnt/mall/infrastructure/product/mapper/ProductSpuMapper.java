package com.lemnt.mall.infrastructure.product.mapper;

import com.lemnt.mall.domain.product.model.StoreFilter;
import com.lemnt.mall.infrastructure.product.dataobject.ProductSpuDO;
import com.lemnt.mall.infrastructure.product.dataobject.StoreProductDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lemnt.mall.domain.product.model.ProductCatalogFilter;

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

    /** 按SKU真实库存聚合进行库存分组分页，分页总数由MyBatis Plus计算。 */
    Page<ProductSpuDO> selectStockPage(Page<ProductSpuDO> page,
            @Param("filter") ProductCatalogFilter filter, @Param("alertStock") int alertStock);

    /** 复杂库存聚合计数；普通CRUD继续使用BaseMapper。 */
    long countStockSpus(@Param("filter") ProductCatalogFilter filter, @Param("alertStock") int alertStock);
}
