package com.lemnt.trade.infrastructure.checkout.dataobject;

import lombok.Data;

@Data
public class CheckoutDO {
    private String orderNo;
    private Long memberId;
    private String requestNo;
    private String fingerprint;
    private java.math.BigDecimal originalAmount;
    private java.math.BigDecimal discountAmount;
    private java.math.BigDecimal payableAmount;
    private Long couponId;
}
