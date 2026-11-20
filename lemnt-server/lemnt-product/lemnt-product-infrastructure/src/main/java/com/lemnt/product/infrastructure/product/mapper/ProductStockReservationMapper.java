package com.lemnt.product.infrastructure.product.mapper;

import com.lemnt.product.infrastructure.product.dataobject.ProductStockReservationDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * ProductStockReservation 表数据访问接口。
 */
@Mapper
public interface ProductStockReservationMapper extends BaseMapper<ProductStockReservationDO> {
    /**
     * 持有商品行锁后读取真实预占行，不使用MyBatis缓存替代加锁。
     */
    ProductStockReservationDO lock(@Param("key") String key, @Param("skuId") Long skuId);
}
