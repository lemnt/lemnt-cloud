package com.lemnt.pay.infrastructure.payment.payment;

import com.lemnt.pay.domain.payment.model.PayChannelConfig;
import com.lemnt.pay.domain.payment.model.PaymentRecord;

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
    boolean closeUnpaid(PaymentRecord payment, PayChannelConfig config);
}
