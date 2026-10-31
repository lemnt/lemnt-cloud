package com.lemnt.trade.application.fulfillment.executor;

import com.lemnt.trade.application.fulfillment.dto.ShipmentTrackDTO;
import com.lemnt.trade.application.order.service.OrderPersistenceService;
import com.lemnt.trade.domain.order.model.OrderRules;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.List;

/** 会员本人订单物流轨迹，核验归属后再按本地运单查询。 */
@Component
@RequiredArgsConstructor
public class MemberOrderTrackQueryExe {
    private final OrderPersistenceService persistence;
    private final ShipmentTrackListQueryExe tracks;

    /** 非本人订单与不存在统一返回空，不查询渠道轨迹。 */
    public List<ShipmentTrackDTO> execute(Long memberId, String orderNo) {
        String no = OrderRules.text(orderNo, 40);
        var order = persistence.findByOrderNo(no);
        if (order == null || memberId == null || !memberId.equals(order.memberId())) {
            return null;
        }
        return tracks.execute(no);
    }
}
