package com.lemnt.trade.application.order.cmd;

import java.io.Serializable;
import java.math.BigDecimal;

/** 后台调整待支付订单应付金额。 */
public record OrderAdjustPriceCmd(String orderNo, BigDecimal totalAmount, Long operatorId)
        implements Serializable {}
