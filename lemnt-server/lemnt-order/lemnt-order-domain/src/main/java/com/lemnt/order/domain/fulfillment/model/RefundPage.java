package com.lemnt.order.domain.fulfillment.model;

import java.util.List;

/**
 * 退款分页。
 */
public record RefundPage(List<RefundRequest> records, long total) {}
