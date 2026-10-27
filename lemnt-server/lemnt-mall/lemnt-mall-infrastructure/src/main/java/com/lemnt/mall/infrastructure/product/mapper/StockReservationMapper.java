package com.lemnt.mall.infrastructure.product.mapper;

import com.lemnt.mall.infrastructure.product.dataobject.StockReservationDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/** StockReservation 表数据访问接口。 */
@Mapper
public interface StockReservationMapper extends BaseMapper<StockReservationDO> {
    /**
     * 释放库存预留。
     */
    @Update("UPDATE mall_stock_reservation SET status='RELEASED', update_time=CURRENT_TIMESTAMP " +
    "WHERE reservation_no=#{reservationNo} AND product_id=#{productId} AND status='RESERVED'")
    int releaseReservation(@Param("reservationNo") String reservationNo, @Param("productId") Long productId);

    /**
     * 确认预留。
     */
    @Update("UPDATE mall_stock_reservation SET status='CONFIRMED', update_time=CURRENT_TIMESTAMP " +
    "WHERE reservation_no=#{reservationNo} AND product_id=#{productId} AND status='RESERVED'")
    int confirmReservation(@Param("reservationNo") String reservationNo, @Param("productId") Long productId);
}
