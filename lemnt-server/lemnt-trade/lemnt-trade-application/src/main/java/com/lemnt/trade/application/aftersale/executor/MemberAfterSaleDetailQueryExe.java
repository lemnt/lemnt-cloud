package com.lemnt.trade.application.aftersale.executor;

import com.lemnt.trade.application.aftersale.converter.AfterSaleDTOConverter;
import com.lemnt.trade.application.aftersale.dto.AfterSaleDTO;
import com.lemnt.trade.domain.aftersale.gateway.AfterSaleGateway;
import com.lemnt.trade.domain.order.model.OrderRules;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 会员本人售后详情，非本人售后与不存在统一返回空。 */
@Component
@RequiredArgsConstructor
public class MemberAfterSaleDetailQueryExe {
    private final AfterSaleGateway afterSales;

    /** 读取后核验归属，避免向其他会员泄露售后。 */
    public AfterSaleDTO execute(Long memberId, String no) {
        var value = afterSales.afterSale(OrderRules.text(no, 40));
        return value == null || memberId == null || !memberId.equals(value.memberId())
                ? null
                : AfterSaleDTOConverter.toDTO(value);
    }
}
