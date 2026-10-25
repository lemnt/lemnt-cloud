package com.lemnt.trade.domain.gateway;

import com.lemnt.trade.domain.model.PaymentRecord;

/** A true result guarantees the provider has atomically closed the unpaid payment.
 * A mere UNPAID query is insufficient: payment could succeed immediately after that query.
 */
public interface PaymentChannelCloseGateway {
    boolean closeUnpaid(PaymentRecord payment);
}
