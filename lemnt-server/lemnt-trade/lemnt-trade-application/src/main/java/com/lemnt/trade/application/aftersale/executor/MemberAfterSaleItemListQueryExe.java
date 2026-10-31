package com.lemnt.trade.application.aftersale.executor;

import com.lemnt.trade.application.aftersale.dto.AfterSaleItemDTO;
import com.lemnt.trade.application.aftersale.service.AfterSaleService;
import com.lemnt.trade.application.order.service.OrderPersistenceService;
import com.lemnt.trade.domain.order.model.OrderRules;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.List;

/** 会员本人订单可售后额度，先核验订单归属再计算剩余额度。 */
@Component
@RequiredArgsConstructor
public class MemberAfterSaleItemListQueryExe {
    private final OrderPersistenceService persistence;
    private final AfterSaleService service;

    /** 非本人订单与不存在统一返回空。 */
    public List<AfterSaleItemDTO> execute(Long memberId, String orderNo) {
        String no = OrderRules.text(orderNo, 40);
        var order = persistence.findByOrderNo(no);
        if (order == null || memberId == null || !memberId.equals(order.memberId())) {
            return null;
        }
        return service.items(no);
    }
}
