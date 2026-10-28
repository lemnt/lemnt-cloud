package com.lemnt.trade.application.aftersale.executor;

import com.lemnt.trade.application.aftersale.converter.AfterSaleDTOConverter;
import com.lemnt.trade.application.aftersale.dto.AfterSaleDTO;
import com.lemnt.trade.application.aftersale.service.AfterSaleService;
import com.lemnt.trade.domain.order.model.TradeOperator;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 商品售后同意执行器。 */
@Component
@RequiredArgsConstructor
public class AfterSaleAgreeCmdExe {
    private final AfterSaleService service;

    /** 执行商品售后流程并返回应用结果。 */
    public AfterSaleDTO execute(String value) {
        return AfterSaleDTOConverter.toDTO(service.agree(value));
    }
    /** 由后台已认证上下文提供操作者，不能取客户端字段。 */
    public AfterSaleDTO executeForAdmin(String value, Long operatorId) {
        return AfterSaleDTOConverter.toDTO(service.agree(value,
                TradeOperator.administrator(operatorId)));
    }
}
