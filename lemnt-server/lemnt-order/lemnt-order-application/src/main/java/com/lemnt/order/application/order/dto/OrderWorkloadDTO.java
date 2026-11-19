package com.lemnt.order.application.order.dto;

/** 后台待办四项业务计数。 */
public record OrderWorkloadDTO(long shipments, long pickups, long afterSales, long withdrawals) {}
