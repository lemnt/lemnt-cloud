package com.lemnt.trade.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lemnt.trade.infrastructure.dataobject.TradeOrderDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import java.time.LocalDateTime;

@Mapper
public interface TradeOrderMapper extends BaseMapper<TradeOrderDO> {
    @org.apache.ibatis.annotations.Select("SELECT CURRENT_TIMESTAMP")
    LocalDateTime databaseNow();

    @Update(
            "UPDATE trade_order SET status=#{targetStatus}, update_time=CURRENT_TIMESTAMP "
                    + "WHERE order_no=#{orderNo} AND status=#{expectedStatus}")
    int transitionStatus(
            @Param("orderNo") String orderNo,
            @Param("expectedStatus") String expectedStatus,
            @Param("targetStatus") String targetStatus);
}
