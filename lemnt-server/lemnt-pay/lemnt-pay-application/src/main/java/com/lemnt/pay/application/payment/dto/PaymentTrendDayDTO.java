package com.lemnt.pay.application.payment.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/** 支付单笔数和各项到账毛额。 */
public record PaymentTrendDayDTO(
        LocalDate date, long payments, BigDecimal paid, BigDecimal recharged) {}
