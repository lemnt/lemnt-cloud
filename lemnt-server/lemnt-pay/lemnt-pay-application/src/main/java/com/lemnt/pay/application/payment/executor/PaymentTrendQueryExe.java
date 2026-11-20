package com.lemnt.pay.application.payment.executor;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.pay.application.payment.dto.PaymentTrendDayDTO;
import com.lemnt.pay.application.payment.qry.PaymentTrendQry;
import com.lemnt.pay.domain.payment.gateway.PaymentTrendGateway;
import com.lemnt.pay.domain.payment.model.PaymentTrendDay;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/** 支付趋势仅汇总已到账记录及原到账金额。 */
@Component
@RequiredArgsConstructor
public class PaymentTrendQueryExe {
    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2);
    private final PaymentTrendGateway gateway;

    /** 日期连续返回，已知区间中的空日保持零值。 */
    public List<PaymentTrendDayDTO> execute(PaymentTrendQry qry) {
        var from = qry.from();
        var to = qry.to();
        if (to.isBefore(from)
                || ChronoUnit.DAYS.between(from, to) >= 366
                || from.getYear() < 1000
                || to.getYear() >= 9999) {
            throw new ServiceException("请选择不超过366天的有效日期范围", 400);
        }
        var until = to.plusDays(1);
        var known =
                gateway.days(from.atStartOfDay(), until.atStartOfDay()).stream()
                        .collect(Collectors.toMap(PaymentTrendDay::date, Function.identity()));
        return from.datesUntil(until)
                .map(
                        date -> {
                            var day = known.get(date);
                            return day == null
                                    ? new PaymentTrendDayDTO(date, 0, ZERO, ZERO)
                                    : new PaymentTrendDayDTO(
                                            date, day.payments(), day.paid(), day.recharged());
                        })
                .toList();
    }
}
