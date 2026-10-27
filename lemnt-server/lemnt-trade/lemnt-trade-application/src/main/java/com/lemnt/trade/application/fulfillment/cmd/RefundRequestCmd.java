package com.lemnt.trade.application.fulfillment.cmd;

/**
 * 退款请求命令。
 */
public record RefundRequestCmd(
        String orderNo,
        String requestNo,
        String reason
) implements java.io.Serializable {}
