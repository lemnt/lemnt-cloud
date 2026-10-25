package com.lemnt.trade.infrastructure.checkout.dataobject;

import lombok.Data;

@Data
public class CouponDO {
    private Long id;
    private Long templateId;
    private Long memberId;
    private String status;
    private String orderNo;
}
