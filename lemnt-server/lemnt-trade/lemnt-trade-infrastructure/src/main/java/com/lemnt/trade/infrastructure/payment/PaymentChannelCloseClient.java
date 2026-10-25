package com.lemnt.trade.infrastructure.payment;

import com.lemnt.trade.domain.model.PaymentRecord;

/** Concrete channel adapters must authenticate responses and enforce bounded network timeouts. */
public interface PaymentChannelCloseClient {
    String channel();
    boolean closeUnpaid(PaymentRecord payment);
}
