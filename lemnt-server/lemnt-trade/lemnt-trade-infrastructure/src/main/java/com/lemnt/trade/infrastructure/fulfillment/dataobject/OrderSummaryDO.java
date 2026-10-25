package com.lemnt.trade.infrastructure.fulfillment.dataobject;

import lombok.Data;

@Data
public class OrderSummaryDO {
    private String orderNo;
    private Long memberId;
    private java.math.BigDecimal totalAmount;
    private String status;
    private java.time.LocalDateTime createTime;
    private Boolean inventoryConfirmed;
    private Boolean activeRefund;
    private String carrierCode;
    private String trackingNo;
    private java.time.LocalDateTime shippedAt;
    private java.time.LocalDateTime receivedAt;
}
