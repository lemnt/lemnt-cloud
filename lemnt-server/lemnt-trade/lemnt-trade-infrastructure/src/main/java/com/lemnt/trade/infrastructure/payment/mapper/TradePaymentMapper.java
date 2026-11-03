package com.lemnt.trade.infrastructure.payment.mapper;

import com.lemnt.trade.infrastructure.payment.dataobject.TradePaymentDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;

/** TradePayment 表数据访问接口。 */
@Mapper
public interface TradePaymentMapper extends BaseMapper<TradePaymentDO> {
    /**
     * 标记关闭。
     */
    int markClosed(@Param("paymentNo") String paymentNo);

    /**
     * 标记成功。
     */
    int markSuccess(
            @Param("paymentNo") String paymentNo,
            @Param("orderNo") String orderNo,
            @Param("channel") String channel,
            @Param("amount") BigDecimal amount,
            @Param("channelTradeNo") String channelTradeNo,
            @Param("notifyId") String notifyId);
}
