package com.lemnt.trade.application.fulfillment.qry;

public record RefundPageQry(String orderNo, String status, int pageNum, int pageSize) {}
