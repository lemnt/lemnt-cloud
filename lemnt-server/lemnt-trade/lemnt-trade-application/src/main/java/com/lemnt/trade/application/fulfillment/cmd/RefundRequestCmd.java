package com.lemnt.trade.application.fulfillment.cmd;

public record RefundRequestCmd(String orderNo, String requestNo, String reason) {}
