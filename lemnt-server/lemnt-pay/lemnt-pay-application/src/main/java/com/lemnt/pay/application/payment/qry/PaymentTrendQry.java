package com.lemnt.pay.application.payment.qry;

import java.time.LocalDate;

/** 支付趋势日期范围，起止自然日均包含。 */
public record PaymentTrendQry(LocalDate from, LocalDate to) {}
