package com.lemnt.order.domain.order.model;

/** 当前待处理的履约、售后及分销提现数量。 */
public record OrderWorkload(long shipments, long pickups, long afterSales, long withdrawals) {}
