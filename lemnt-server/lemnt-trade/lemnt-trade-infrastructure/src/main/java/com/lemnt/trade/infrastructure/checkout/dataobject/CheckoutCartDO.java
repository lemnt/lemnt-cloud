package com.lemnt.trade.infrastructure.checkout.dataobject;

import lombok.Data;

@Data
public class CheckoutCartDO {
    private String orderNo;
    private Long cartId;
    private Long cartVersion;
}
