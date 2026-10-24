package com.lemnt.trade.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lemnt.trade.infrastructure.dataobject.TradePaymentDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface TradePaymentMapper extends BaseMapper<TradePaymentDO> {
    @Update("UPDATE trade_payment SET status='SUCCESS', channel_trade_no=#{channelTradeNo}, notify_id=#{notifyId}, update_time=CURRENT_TIMESTAMP " +
            "WHERE payment_no=#{paymentNo} AND status='WAITING'")
    int markSuccess(@Param("paymentNo") String paymentNo, @Param("channelTradeNo") String channelTradeNo,
                    @Param("notifyId") String notifyId);
}