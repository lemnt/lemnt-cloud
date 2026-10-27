package com.lemnt.trade.domain.payment.gateway;

import com.lemnt.trade.domain.payment.model.PaymentRecord;

/** A true result guarantees the provider has atomically closed the unpaid payment.
 * A mere UNPAID query is insufficient: payment could succeed immediately after that query.
 */
public interface PaymentChannelCloseGateway {
    /**
     * 关闭未支付。
     */
    boolean closeUnpaid(PaymentRecord payment);
}
