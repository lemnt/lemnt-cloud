package com.lemnt.trade.infrastructure.checkout.dataobject;

import lombok.Data;

@Data
public class CouponTemplateDO {
    private Long id;
    private String code;
    private String name;
    private String kind;
    private java.math.BigDecimal threshold;
    private java.math.BigDecimal fixedAmount;
    private Integer discountPercent;
    private java.math.BigDecimal discountLimit;
    private Integer totalCount;
    private Integer issuedCount;
    private java.time.LocalDateTime validFrom;
    private java.time.LocalDateTime validUntil;
    private Boolean enabled;
}
