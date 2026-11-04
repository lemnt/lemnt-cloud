package com.lemnt.pay.application.payment.executor;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.pay.application.payment.converter.PaymentDTOConverter;
import com.lemnt.pay.application.payment.dto.PaymentDTO;
import com.lemnt.pay.application.payment.qry.PaymentPageQry;
import com.lemnt.pay.domain.payment.gateway.TradePaymentGateway;
import com.lemnt.pay.domain.payment.model.PaymentChannel;
import com.lemnt.pay.domain.payment.model.PaymentFilter;
import com.lemnt.pay.domain.payment.model.PaymentRules;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.Set;

/**
 * 支付单管理查询执行器。
 */
@Component
@RequiredArgsConstructor
public class PaymentPageQueryExe {
    private final TradePaymentGateway gateway;

    /**
     * 按精确条件分页查询，不返回内部通知标识。
     */
    public PageResult<PaymentDTO> execute(PaymentPageQry query) {
        if (query == null
            || query.pageNum() < 1
            || query.pageNum() > 1000000
            || query.pageSize() < 1
            || query.pageSize() > 100) {
            throw new IllegalArgumentException("分页参数不合法");
        }
        String paymentNo = text(query.paymentNo(), 40);
        String orderNo = text(query.orderNo(), 40);
        String channel = text(query.channel(), 32);
        String status = text(query.status(), 20);
        if (status != null && !Set.of("WAITING", "SUCCESS", "CLOSED").contains(status)) {
            throw new IllegalArgumentException("状态参数不合法");
        }
        if (channel != null) {
            PaymentChannel.valueOf(channel);
        }
        var page =
            gateway.page(
                new PaymentFilter(
                    paymentNo,
                    orderNo,
                    channel,
                    status,
                    query.pageNum(),
                    query.pageSize()));
        return PageResult.of(
            page.getRecords().stream().map(PaymentDTOConverter::toDTO).toList(),
            page.getTotal());
    }

    private String text(String value, int max) {
        return value == null || value.isBlank() ? null : PaymentRules.text(value, max);
    }
}
