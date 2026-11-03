package com.lemnt.order.application.fulfillment.cmd;

import java.io.Serializable;

/**
 * 退款请求命令。
 */
public record RefundRequestCmd(String orderNo, String requestNo, String reason)
        implements Serializable {}
