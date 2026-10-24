package com.lemnt.mall.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lemnt.mall.infrastructure.dataobject.StockReservationDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface StockReservationMapper extends BaseMapper<StockReservationDO> {
    @Update("UPDATE mall_stock_reservation SET status='RELEASED', update_time=CURRENT_TIMESTAMP " +
            "WHERE reservation_no=#{reservationNo} AND product_id=#{productId} AND status='RESERVED'")
    int releaseReservation(@Param("reservationNo") String reservationNo, @Param("productId") Long productId);

    @Update("UPDATE mall_stock_reservation SET status='CONFIRMED', update_time=CURRENT_TIMESTAMP " +
            "WHERE reservation_no=#{reservationNo} AND product_id=#{productId} AND status='RESERVED'")
    int confirmReservation(@Param("reservationNo") String reservationNo, @Param("productId") Long productId);
}