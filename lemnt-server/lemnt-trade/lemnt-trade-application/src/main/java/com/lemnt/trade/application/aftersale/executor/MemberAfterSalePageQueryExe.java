package com.lemnt.trade.application.aftersale.executor;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.trade.application.aftersale.converter.AfterSaleDTOConverter;
import com.lemnt.trade.application.aftersale.dto.AfterSaleDTO;
import com.lemnt.trade.application.aftersale.qry.MemberAfterSalePageQry;
import com.lemnt.trade.domain.aftersale.gateway.AfterSaleGateway;
import com.lemnt.trade.domain.aftersale.model.AfterSaleFilter;
import com.lemnt.trade.domain.order.model.OrderRules;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.Set;

/** 会员本人售后分页，SQL 层按 member_id 过滤。 */
@Component
@RequiredArgsConstructor
public class MemberAfterSalePageQueryExe {
    private final AfterSaleGateway afterSales;

    /** 校验分页与状态后查询本人售后。 */
    public PageResult<AfterSaleDTO> execute(Long memberId, MemberAfterSalePageQry qry) {
        if (memberId == null) {
            throw new IllegalArgumentException("会员身份缺失");
        }
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
        var page = afterSales.afterSalePage(new AfterSaleFilter(no, status, memberId,
                (qry.pageNum() - 1) * qry.pageSize(), qry.pageSize()));
        return PageResult.of(page.getRecords().stream().map(AfterSaleDTOConverter::toDTO).toList(), page.getTotal());
    }
}
