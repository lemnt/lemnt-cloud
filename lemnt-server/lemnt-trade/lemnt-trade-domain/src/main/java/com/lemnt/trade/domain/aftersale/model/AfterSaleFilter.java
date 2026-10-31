package com.lemnt.trade.domain.aftersale.model;

/** 售后分页筛选条件。memberId 用于会员本人售后，后台传空。 */
public record AfterSaleFilter(String orderNo, String status, Long memberId, int offset, int limit) {
}
