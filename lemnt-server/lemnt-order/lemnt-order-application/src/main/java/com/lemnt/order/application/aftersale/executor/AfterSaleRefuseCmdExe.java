package com.lemnt.order.application.aftersale.executor;

import com.lemnt.order.application.aftersale.cmd.AfterSaleRefuseCmd;
import com.lemnt.order.application.aftersale.converter.AfterSaleDTOConverter;
import com.lemnt.order.application.aftersale.dto.AfterSaleDTO;
import com.lemnt.order.application.aftersale.service.AfterSaleService;
import com.lemnt.order.domain.order.model.TradeOperator;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 商品售后拒收执行器。
 */
@Component
@RequiredArgsConstructor
public class AfterSaleRefuseCmdExe {
    private final AfterSaleService service;

    /**
     * 执行商品售后流程并返回应用结果。
     */
    public AfterSaleDTO execute(AfterSaleRefuseCmd value) {
        return AfterSaleDTOConverter.toDTO(service.refuse(value));
    }

    /**
     * 由后台已认证上下文提供操作者，不能取客户端字段。
     */
    public AfterSaleDTO executeForAdmin(AfterSaleRefuseCmd value, Long operatorId) {
        return AfterSaleDTOConverter.toDTO(
            service.refuse(value, TradeOperator.administrator(operatorId)));
    }
}
