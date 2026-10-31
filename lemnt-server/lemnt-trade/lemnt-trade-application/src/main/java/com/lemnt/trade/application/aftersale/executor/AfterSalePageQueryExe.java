package com.lemnt.trade.application.aftersale.executor;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.trade.application.aftersale.converter.AfterSaleDTOConverter;
import com.lemnt.trade.application.aftersale.dto.AfterSaleDTO;
import com.lemnt.trade.application.aftersale.qry.AfterSalePageQry;
import com.lemnt.trade.domain.aftersale.gateway.AfterSaleGateway;
import com.lemnt.trade.domain.aftersale.model.AfterSaleFilter;
import com.lemnt.trade.domain.order.model.OrderRules;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.Set;

/** 售后分页查询。 */
@Component
@RequiredArgsConstructor
public class AfterSalePageQueryExe {
    private final AfterSaleGateway afterSales;

    /** 按精确条件查询售后，不混用支付退款交易状态。 */
    public PageResult<AfterSaleDTO> execute(AfterSalePageQry qry) {
        if (qry == null || qry.pageNum() < 1 || qry.pageNum() > 1000000
                || qry.pageSize() < 1 || qry.pageSize() > 100) {
            throw new IllegalArgumentException("分页参数不合法");
        }
        String no = qry.orderNo() == null || qry.orderNo().isBlank() ? null : OrderRules.text(qry.orderNo(), 40);
        String status = qry.status() == null || qry.status().isBlank() ? null : qry.status();
        if (status != null && !Set.of("REQUESTED", "REJECTED", "AWAITING_RETURN", "RETURNED", "WAIT_REFUND",
                "RETURN_REJECTED", "REFUNDING", "REFUNDED", "CANCELLED").contains(status)) {
            throw new IllegalArgumentException("售后状态不合法");
        }
        var page = afterSales.afterSalePage(new AfterSaleFilter(no, status,
                (qry.pageNum() - 1) * qry.pageSize(), qry.pageSize()));
        return PageResult.of(page.getRecords().stream().map(AfterSaleDTOConverter::toDTO).toList(), page.getTotal());
    }
}
