package com.lemnt.pay.domain.payment.gateway;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.pay.domain.payment.model.PaymentFilter;
import com.lemnt.pay.domain.payment.model.PaymentRecord;
import com.lemnt.pay.domain.payment.model.VerifiedPaymentNotification;

import java.math.BigDecimal;
import java.util.List;

/**
 * 交易支付领域网关。
 */
public interface TradePaymentGateway {
    /** 支付库内串行资金确认及退款额度，不依赖远程订单行锁。 */
    PaymentRecord lockByPaymentNo(String paymentNo);

    /** 按条件分页查询支付单。 */
    PageResult<PaymentRecord> page(PaymentFilter filter);

    /**
     * 查找按支付编号。
     */
    PaymentRecord findByPaymentNo(String paymentNo);

    /**
     * 查找按订单编号及支付渠道。
     */
    PaymentRecord findByOrderNoAndChannel(String orderNo, String channel);

    /**
     * 创建待支付记录。
     */
    PaymentRecord createWaiting(
            String paymentNo, String orderNo, String channel, String appKey, BigDecimal amount);

    /**
     * 查找按订单编号。
     */
    List<PaymentRecord> findByOrderNo(String orderNo);

    /**
     * 标记关闭。
     */
    boolean markClosed(String paymentNo);

    /**
     * 标记成功。
     */
    boolean markSuccess(VerifiedPaymentNotification notification);
}
