package com.lemnt.trade.application.fulfillment.executor;

import com.lemnt.trade.application.fulfillment.dto.MemberOrderDetailDTO;
import com.lemnt.trade.application.order.service.OrderPersistenceService;
import com.lemnt.trade.domain.order.model.OrderRules;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 会员本人订单详情，非本人订单与不存在统一返回空。 */
@Component
@RequiredArgsConstructor
public class MemberOrderDetailQueryExe {
    private final OrderDetailQueryExe detail;
    private final OrderPersistenceService persistence;

    /** 读取详情与下单时间后核验归属，避免向其他会员泄露订单。 */
    public MemberOrderDetailDTO execute(Long memberId, String orderNo) {
        String no = OrderRules.text(orderNo, 40);
        var order = persistence.findByOrderNo(no);
        if (order == null || memberId == null || !memberId.equals(order.memberId())) {
            return null;
        }
        var value = detail.execute(no);
        return value == null
                ? null
                : new MemberOrderDetailDTO(value.orderNo(), value.status(), value.totalAmount(),
                        order.createTime(), value.activeRefund(), value.items(), value.shipment(),
                        value.commentStatus(), value.finishTime());
    }
}
