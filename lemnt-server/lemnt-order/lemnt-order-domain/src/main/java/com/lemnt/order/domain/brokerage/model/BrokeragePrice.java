package com.lemnt.order.domain.brokerage.model;

import java.math.BigDecimal;

/** 一级单件佣金预估的当前标价和固定金额，单位元。 */
public record BrokeragePrice(BigDecimal price, BigDecimal firstFixed) {}
