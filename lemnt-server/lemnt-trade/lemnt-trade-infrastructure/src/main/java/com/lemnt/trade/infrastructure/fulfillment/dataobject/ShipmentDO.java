package com.lemnt.trade.infrastructure.fulfillment.dataobject;

import lombok.Data;

@Data
public class ShipmentDO {
    private String orderNo;
    private String requestNo;
    private String fingerprint;
    private String carrierCode;
    private String trackingNo;
    private String recipientName;
    private String phone;
    private String address;
    private java.time.LocalDateTime shippedAt;
    private java.time.LocalDateTime receivedAt;
}
