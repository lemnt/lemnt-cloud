package com.lemnt.trade.application.fulfillment.cmd;

/**
 * 发货创建命令。
 */
public record ShipmentCreateCmd(
        String orderNo,
        String requestNo,
        String carrierCode,
        String trackingNo,
        String recipientName,
        String phone,
        String address
) implements java.io.Serializable {}
