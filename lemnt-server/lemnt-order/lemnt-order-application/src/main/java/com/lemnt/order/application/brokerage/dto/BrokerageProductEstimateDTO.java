package com.lemnt.order.application.brokerage.dto;

import java.math.BigDecimal;

/** 一级单件佣金预估。 */
public record BrokerageProductEstimateDTO(boolean enabled, BigDecimal min, BigDecimal max) {}
