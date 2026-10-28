package com.lemnt.trade.application.paymentrefund.executor;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.trade.application.paymentrefund.converter.PaymentRefundDTOConverter;
import com.lemnt.trade.application.paymentrefund.dto.PaymentRefundDTO;
import com.lemnt.trade.application.paymentrefund.qry.PaymentRefundPageQry;
import com.lemnt.trade.domain.order.model.OrderRules;
import com.lemnt.trade.domain.paymentrefund.gateway.PaymentRefundGateway;
import com.lemnt.trade.domain.paymentrefund.model.PaymentRefundFilter;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.Set;

/** 支付退款管理查询执行器。 */
@Component
@RequiredArgsConstructor
public class PaymentRefundPageQueryExe {
    private final PaymentRefundGateway gateway;

    /** 按精确条件分页查询，不返回内部通知标识。 */
    public PageResult<PaymentRefundDTO> execute(PaymentRefundPageQry query) {
        if (query == null || query.pageNum() < 1 || query.pageNum() > 1000000
                || query.pageSize() < 1 || query.pageSize() > 100) {
            throw new IllegalArgumentException("分页参数不合法");
        }
        String afterSaleNo = text(query.afterSaleNo(), 40);
        String paymentNo = text(query.paymentNo(), 40);
        String status = text(query.status(), 20);
        if (status != null && !Set.of("WAITING", "SUCCESS", "FAILED").contains(status)) {
            throw new IllegalArgumentException("状态参数不合法");
        }
        var page = gateway.page(new PaymentRefundFilter(
                afterSaleNo, paymentNo, status, query.pageNum(), query.pageSize()));
        return PageResult.of(
                page.getRecords().stream().map(PaymentRefundDTOConverter::toDTO).toList(), page.getTotal());
    }

    private String text(String value, int max) {
        return value == null || value.isBlank() ? null : OrderRules.text(value, max);
    }
}
