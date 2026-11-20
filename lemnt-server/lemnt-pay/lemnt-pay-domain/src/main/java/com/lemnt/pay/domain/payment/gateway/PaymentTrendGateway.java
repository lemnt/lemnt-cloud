package com.lemnt.pay.domain.payment.gateway;

import com.lemnt.pay.domain.payment.model.PaymentTrendDay;

import java.time.LocalDateTime;
import java.util.List;

public interface PaymentTrendGateway {
    /** 起点包含，终点排除，真实到账时间未知的历史记录不计入。 */
    List<PaymentTrendDay> days(LocalDateTime from, LocalDateTime until);
}
