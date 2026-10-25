package com.lemnt.trade.application.fulfillment.cmd;

public record ShipmentCreateCmd(
        String orderNo,
        String requestNo,
        String carrierCode,
        String trackingNo,
        String recipientName,
        String phone,
        String address) {}
