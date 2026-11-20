package com.lemnt.pay.domain.payment.gateway;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.pay.domain.payment.model.PaymentFilter;
import com.lemnt.pay.domain.payment.model.PaymentOrder;
import com.lemnt.pay.domain.payment.model.PaymentRecord;
import com.lemnt.pay.domain.payment.model.VerifiedPaymentNotification;

import java.math.BigDecimal;
import java.util.List;

/**
 * 交易支付领域网关。
 */
public interface TradePaymentGateway {
    /**
     * 支付库内串行资金确认及退款额度，不依赖远程订单行锁。
     */
    PaymentRecord lockByPaymentNo(String paymentNo);

    /** 确认渠道结果时先锁主单，再锁原渠道尝试。 */
    PaymentRecord lockForConfirmation(String orderNo, String paymentNo);

    /** 读取业务支付主单，用于支付详情及收银台核对。 */
    PaymentOrder findPaymentOrder(String orderNo);

    /** 只在原业务已关闭后收口主单，保留已收款和待对账事实。 */
    void closePaymentOrder(String orderNo);

    /**
     * 按条件分页查询支付单。
     */
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
     * 创建待支付记录。memberId 来自订单可信快照，落库用于资金归属审计。
     */
    PaymentRecord createWaiting(
        String paymentNo,
        String orderNo,
        Long memberId,
        String channel,
        String appKey,
        BigDecimal amount);

    /**
     * 查找按订单编号。
     */
    List<PaymentRecord> findByOrderNo(String orderNo);

    /** 按主键扫描有应用归属的待支付渠道单。 */
    List<PaymentRecord> waitingForQuery(long afterId, int limit);

    /** 支付库当前 Unix 秒，和过期时间使用同一时钟。 */
    long databaseEpochSecond();

    /** 新版微信尝试中到期且尚待确认的记录。 */
    List<PaymentRecord> expiredWaiting(long afterId, int limit);

    /**
     * 标记关闭。
     */
    boolean markClosed(String paymentNo);

    /**
     * 标记成功。
     */
    boolean markSuccess(VerifiedPaymentNotification notification);
}
