package com.lemnt.mall.infrastructure.product.mapper;

import com.lemnt.mall.infrastructure.product.dataobject.ProductSkuDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/** Product 表数据访问接口。 */
@Mapper
public interface ProductSkuMapper extends BaseMapper<ProductSkuDO> {

    /**
     * 锁定按编号。
     */
    ProductSkuDO lockById(Long id);

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
