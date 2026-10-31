package com.lemnt.trade.application.fulfillment.executor;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.trade.application.fulfillment.dto.MemberOrderSummaryDTO;
import com.lemnt.trade.application.fulfillment.dto.OrderItemDTO;
import com.lemnt.trade.application.fulfillment.qry.MemberOrderPageQry;
import com.lemnt.trade.application.order.service.OrderPersistenceService;
import com.lemnt.trade.domain.fulfillment.model.FulfillmentFilter;
import com.lemnt.trade.domain.order.gateway.TradeOrderGateway;
import com.lemnt.trade.domain.order.model.TradeOrderItem;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Set;

/** 会员本人订单分页，SQL 层按 member_id 过滤，明细读取下单快照。 */
@Component
@RequiredArgsConstructor
public class MemberOrderPageQueryExe {
    private static final Set<String> STATUSES = Set.of("RESERVING", "PENDING_PAYMENT", "PAID", "SHIPPED",
            "COMPLETED", "REFUNDED", "CLOSED");

    private final TradeOrderGateway orders;
    private final OrderPersistenceService persistence;

    /** 校验分页与状态后查询本人订单。 */
    public PageResult<MemberOrderSummaryDTO> execute(Long memberId, MemberOrderPageQry qry) {
        if (memberId == null) {
            throw new IllegalArgumentException("会员身份缺失");
        }
        if (qry == null || qry.pageNum() < 1 || qry.pageNum() > 1000000
                || qry.pageSize() < 1 || qry.pageSize() > 100) {
            throw new IllegalArgumentException("分页参数不合法");
        }
        String status = qry.status() == null || qry.status().isBlank() ? null : qry.status();
        if (status != null && !STATUSES.contains(status)) {
            throw new IllegalArgumentException("状态参数不合法");
        }
        var page = orders.orders(new FulfillmentFilter(null, status, qry.commentStatus(), memberId,
                (qry.pageNum() - 1) * qry.pageSize(), qry.pageSize()));
        var records = page.records().stream().map(summary -> {
            var order = persistence.findByOrderNo(summary.orderNo());
            return new MemberOrderSummaryDTO(summary.orderNo(), summary.status(), summary.totalAmount(),
                    summary.createTime(),
                    order == null ? null : order.commentStatus(),
                    order == null ? null : order.finishTime(),
                    summary.activeRefund(),
                    order == null ? List.of() : order.items().stream().map(this::item).toList());
        }).toList();
        return PageResult.of(records, page.total());
    }

    private OrderItemDTO item(TradeOrderItem value) {
        return new OrderItemDTO(value.productId(), value.productCode(), value.productName(), value.unitPrice(),
                value.quantity(), value.lineAmount(), value.spuId(), value.spuName(),
                value.properties() == null ? null : value.properties().stream()
                        .map(property -> new OrderItemDTO.Property(property.propertyId(), property.propertyName(),
                                property.valueId(), property.valueName())).toList(),
                value.picUrl(), value.orderItemId(), value.commentStatus());
    }
}
