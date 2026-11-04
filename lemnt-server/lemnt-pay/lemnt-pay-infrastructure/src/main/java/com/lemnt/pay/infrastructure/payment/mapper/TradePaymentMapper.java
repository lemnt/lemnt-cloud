package com.lemnt.pay.infrastructure.payment.mapper;

import com.lemnt.pay.infrastructure.payment.dataobject.TradePaymentDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;

/**
 * TradePayment 表数据访问接口。
 */
@Mapper
public interface TradePaymentMapper extends BaseMapper<TradePaymentDO> {
    /**
     * 相同订单/渠道只保留原支付编号，不覆盖历史资金内容。
     */
    int createWaiting(TradePaymentDO row);

    TradePaymentDO lockByPaymentNo(@Param("paymentNo") String paymentNo);

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
