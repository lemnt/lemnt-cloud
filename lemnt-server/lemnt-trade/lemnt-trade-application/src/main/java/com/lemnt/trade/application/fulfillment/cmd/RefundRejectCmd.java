package com.lemnt.trade.application.fulfillment.cmd;

import java.io.Serializable;

/**
 * 退款拒绝命令。
 */
public record RefundRejectCmd(String refundNo, String reason) implements Serializable {}
