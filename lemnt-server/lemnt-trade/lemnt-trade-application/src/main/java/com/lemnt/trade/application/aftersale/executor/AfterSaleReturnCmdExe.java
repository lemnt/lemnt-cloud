package com.lemnt.trade.application.aftersale.executor;

import com.lemnt.trade.application.aftersale.cmd.AfterSaleReturnCmd;
import com.lemnt.trade.application.aftersale.converter.AfterSaleDTOConverter;
import com.lemnt.trade.application.aftersale.dto.AfterSaleDTO;
import com.lemnt.trade.application.aftersale.service.AfterSaleService;
import com.lemnt.trade.domain.order.model.TradeOperator;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 商品售后登记退货执行器。 */
@Component
@RequiredArgsConstructor
public class AfterSaleReturnCmdExe {
    private final AfterSaleService service;

    /** 执行商品售后流程并返回应用结果。 */
    public AfterSaleDTO execute(AfterSaleReturnCmd value) {
        return AfterSaleDTOConverter.toDTO(service.registerReturn(value));
    }
    /** 由后台已认证上下文提供操作者，不能取客户端字段。 */
    public AfterSaleDTO executeForAdmin(AfterSaleReturnCmd value, Long operatorId) {
        return AfterSaleDTOConverter.toDTO(service.registerReturn(value,
                TradeOperator.administrator(operatorId)));
    }

    /** 会员本人登记退货运单，领域层再次核验售后归属与阶段。 */
    public AfterSaleDTO executeForMember(AfterSaleReturnCmd value, Long memberId) {
        return AfterSaleDTOConverter.toDTO(service.registerReturn(value, TradeOperator.member(memberId)));
    }
}
