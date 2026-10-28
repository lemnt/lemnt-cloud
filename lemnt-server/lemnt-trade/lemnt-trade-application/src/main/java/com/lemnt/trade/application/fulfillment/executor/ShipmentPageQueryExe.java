package com.lemnt.trade.application.fulfillment.executor;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.trade.application.fulfillment.dto.ShipmentSummaryDTO;
import com.lemnt.trade.application.fulfillment.qry.ShipmentPageQry;
import com.lemnt.trade.domain.fulfillment.gateway.ShipmentGateway;
import com.lemnt.trade.domain.fulfillment.model.ShipmentFilter;
import com.lemnt.trade.domain.order.model.OrderRules;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.Set;

/** 物流单分页查询。 */
@Component
@RequiredArgsConstructor
public class ShipmentPageQueryExe {
    private final ShipmentGateway shipments;

    /** 查询本地发货与收货记录，列表不含收件人信息。 */
    public PageResult<ShipmentSummaryDTO> execute(ShipmentPageQry qry) {
        if (qry == null || qry.pageNum() < 1 || qry.pageNum() > 1000000
                || qry.pageSize() < 1 || qry.pageSize() > 100) {
            throw new IllegalArgumentException("分页参数不合法");
        }
        String status = text(qry.status(), 20);
        if (status != null && !Set.of("SHIPPED", "RECEIVED").contains(status)) {
            throw new IllegalArgumentException("物流状态不合法");
        }
        var page = shipments.page(new ShipmentFilter(text(qry.orderNo(), 40), text(qry.carrierCode(), 40),
                text(qry.trackingNo(), 100), status, qry.pageNum(), qry.pageSize()));
        return PageResult.of(page.getRecords().stream().map(value -> new ShipmentSummaryDTO(value.orderNo(),
                value.carrierCode(), value.trackingNo(), value.shippedAt(), value.receivedAt())).toList(), page.getTotal());
    }

    private String text(String value, int max) {
        return value == null || value.isBlank() ? null : OrderRules.text(value, max);
    }
}
