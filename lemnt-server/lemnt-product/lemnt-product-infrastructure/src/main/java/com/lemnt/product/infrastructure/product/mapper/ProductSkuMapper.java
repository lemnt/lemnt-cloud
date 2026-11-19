package com.lemnt.product.infrastructure.product.mapper;

import com.lemnt.product.domain.product.model.ProductBrokeragePrice;
import com.lemnt.product.domain.product.model.ProductFixedCommission;
import com.lemnt.product.infrastructure.product.dataobject.ProductSkuDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Product 表数据访问接口。
 */
@Mapper
public interface ProductSkuMapper extends BaseMapper<ProductSkuDO> {

    /**
     * 锁定按编号。
     */
    ProductSkuDO lockById(Long id);

    /** 以同一次查询读取 SPU 分佣方式和 SKU 金额，避免商品编辑造成配置撕裂。 */
    List<ProductFixedCommission> fixedCommissions(@Param("ids") List<Long> ids);

    /** 同一查询读取标价和固定佣金，避免商品同时编辑造成快照撕裂。 */
    List<ProductBrokeragePrice> brokeragePrices(@Param("spuId") Long spuId);

    /**
     * 查找可售商品按编号集合。
     */
    List<ProductSkuDO> findSellableByIds(@Param("ids") List<Long> ids);

    /**
     * 查找可售商品按编号。
     */
    ProductSkuDO findSellableById(Long id);

    /**
     * 预留库存。
     */
    int reserveStock(@Param("id") Long id, @Param("quantity") Integer quantity);
}
