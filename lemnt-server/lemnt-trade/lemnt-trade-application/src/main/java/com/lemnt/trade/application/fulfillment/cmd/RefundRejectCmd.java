package com.lemnt.trade.application.fulfillment.cmd;

/**
 * 退款拒绝命令。
 */
public record RefundRejectCmd(
        String refundNo,
        String reason
) implements java.io.Serializable {}
