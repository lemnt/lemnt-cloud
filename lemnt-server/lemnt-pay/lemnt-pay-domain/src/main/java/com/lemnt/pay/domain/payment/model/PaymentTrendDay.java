package com.lemnt.pay.domain.payment.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/** 按首次支付到账日期汇总的单日资金事实，金额为人民币元。 */
public record PaymentTrendDay(
        LocalDate date, long payments, BigDecimal paid, BigDecimal recharged) {}
