package com.lemnt.trade.domain.aftersale.model;

/** 售后分页筛选条件。 */
public record AfterSaleFilter(String orderNo, String status, int offset, int limit) {
}
