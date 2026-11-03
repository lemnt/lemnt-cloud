package com.lemnt.order.application.fulfillment.executor;

import com.lemnt.order.application.fulfillment.dto.MemberOrderDetailDTO;
import com.lemnt.order.application.order.service.OrderPersistenceService;
import com.lemnt.order.domain.delivery.gateway.PickUpStoreGateway;
import com.lemnt.order.domain.order.model.OrderRules;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 会员本人订单详情，非本人订单与不存在统一返回空。 */
@Component
@RequiredArgsConstructor
public class MemberOrderDetailQueryExe {
    private final OrderDetailQueryExe detail;
    private final OrderPersistenceService persistence;
    private final PickUpStoreGateway pickUpStores;

    /** 读取详情与下单时间后核验归属，避免向其他会员泄露订单。 */
    public MemberOrderDetailDTO execute(Long memberId, String orderNo) {
        String no = OrderRules.text(orderNo, 40);
        var order = persistence.findByOrderNo(no);
        if (order == null || memberId == null || !memberId.equals(order.memberId())) {
            return null;
        }
        var value = detail.execute(no);
        if (value == null) {
            return null;
        }
        var delivery = order.delivery();
        var store =
                delivery == null || delivery.pickUpStoreId() == null
                        ? null
                        : pickUpStores.find(delivery.pickUpStoreId());
        var displayStatus =
                "PAID".equals(value.status())
                                && delivery != null
                                && Integer.valueOf(2).equals(delivery.deliveryType())
                        ? "AWAITING_PICKUP"
                        : value.status();
        return new MemberOrderDetailDTO(
                value.orderNo(),
                displayStatus,
                value.totalAmount(),
                order.createTime(),
                value.activeRefund(),
                value.items(),
                value.shipment(),
                value.commentStatus(),
                value.finishTime(),
                store == null ? null : store.name(),
                store == null ? null : store.phone(),
                store == null ? null : store.detailAddress(),
                delivery == null ? null : delivery.pickUpVerifyCode());
    }
}
