package com.lemnt.trade.infrastructure.payment.mapper;

import com.lemnt.trade.infrastructure.payment.dataobject.TradePaymentDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;

/** TradePayment 表数据访问接口。 */
@Mapper
public interface TradePaymentMapper extends BaseMapper<TradePaymentDO> {
    /**
     * 标记关闭。
     */
    @Update("UPDATE trade_payment SET status='CLOSED', update_time=CURRENT_TIMESTAMP "
            + "WHERE payment_no=#{paymentNo} AND status='WAITING'")
    int markClosed(@Param("paymentNo") String paymentNo);

    /**
     * 标记成功。
     */
    @Update("UPDATE trade_payment SET status='SUCCESS', channel_trade_no=#{channelTradeNo}, notify_id=#{notifyId}, update_time=CURRENT_TIMESTAMP " +
    "WHERE payment_no=#{paymentNo} AND order_no=#{orderNo} AND channel=#{channel} AND amount=#{amount} AND status='WAITING'")
    int markSuccess(@Param("paymentNo") String paymentNo, @Param("orderNo") String orderNo,
            @Param("channel") String channel, @Param("amount") BigDecimal amount,
            @Param("channelTradeNo") String channelTradeNo, @Param("notifyId") String notifyId);
}
