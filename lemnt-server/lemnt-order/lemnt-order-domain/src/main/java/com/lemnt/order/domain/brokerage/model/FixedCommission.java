package com.lemnt.order.domain.brokerage.model;

import java.math.BigDecimal;

/** SKU 每件一级和二级固定佣金，单位元。显式零表示无佣金。 */
public record FixedCommission(BigDecimal first, BigDecimal second) {}
