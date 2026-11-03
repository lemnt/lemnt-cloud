package com.lemnt.order.application.aftersale.executor;

import com.lemnt.order.application.aftersale.converter.AfterSaleDTOConverter;
import com.lemnt.order.application.aftersale.dto.AfterSaleDTO;
import com.lemnt.order.domain.aftersale.gateway.AfterSaleGateway;
import com.lemnt.order.domain.order.model.OrderRules;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 售后详情查询。 */
@Component
@RequiredArgsConstructor
public class AfterSaleDetailQueryExe {
    private final AfterSaleGateway afterSales;

    /** 查询完整审批和退货信息。 */
    public AfterSaleDTO execute(String no) {
        return AfterSaleDTOConverter.toDTO(afterSales.afterSale(OrderRules.text(no, 40)));
    }
}
