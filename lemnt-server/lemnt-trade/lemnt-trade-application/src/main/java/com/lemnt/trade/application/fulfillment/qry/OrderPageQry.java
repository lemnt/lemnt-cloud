package com.lemnt.trade.application.fulfillment.qry;

public record OrderPageQry(String orderNo, String status, int pageNum, int pageSize) {}
