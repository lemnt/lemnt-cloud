package com.lemnt.trade.infrastructure.payment.payment;

import com.lemnt.trade.domain.payment.model.PaymentRecord;

/**
 * Concrete channel adapters must authenticate responses and enforce bounded network timeouts.
 */
public interface PaymentChannelCloseClient {
    /**
     * 渠道。
     */
    String channel();

    /**
     * 关闭未支付。
     */
    boolean closeUnpaid(PaymentRecord payment);
}
