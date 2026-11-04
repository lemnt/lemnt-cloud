package com.lemnt.order.application.promotion.executor;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.order.application.promotion.dto.OrderPromotionDetailDTO;
import com.lemnt.order.domain.order.gateway.TradeOrderGateway;
import com.lemnt.order.domain.order.model.OrderRules;
import com.lemnt.order.domain.promotion.gateway.OrderPromotionGateway;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 订单优惠查询只读trade快照，会员入口严格校验归属。
 */
@Component
@RequiredArgsConstructor
public class OrderPromotionDetailQueryExe {
    private final TradeOrderGateway orders;
    private final OrderPromotionGateway promotions;

    public OrderPromotionDetailDTO execute(String no) {
        return read(null, no);
    }

    public OrderPromotionDetailDTO execute(Long memberId, String no) {
        if (memberId == null || memberId <= 0) {
            throw new ServiceException("订单不存在", 400);
        }
        return read(memberId, no);
    }

    private OrderPromotionDetailDTO read(Long memberId, String no) {
        var order = orders.findHeader(OrderRules.text(no, 40));
        if (order == null || (memberId != null && !memberId.equals(order.memberId()))) {
            throw new ServiceException("订单不存在", 400);
        }
        var rows =
            promotions.find(order.id()).stream()
                .map(
                    p ->
                        new OrderPromotionDetailDTO.Promotion(
                            p.promotionType(),
                            p.promotionName(),
                            p.discountAmount(),
                            p.deliveryDiscountPrice(),
                            p.usePoint(),
                            p.givePoint(),
                            p.giveCouponTemplateCounts(),
                            p.items().stream()
                                .map(
                                    i ->
                                        new OrderPromotionDetailDTO
                                            .Item(
                                            i.orderItemId(),
                                            i.discountAmount(),
                                            i
                                                .deliveryDiscountPrice(),
                                            i.usePoint(),
                                            i.givePoint()))
                                .toList()))
                .toList();
        return new OrderPromotionDetailDTO(
            order.orderNo(),
            order.pricingSnapshotStatus(),
            new OrderPromotionDetailDTO.Summary(
                order.originalAmount(),
                order.discountAmount(),
                order.deliveryOriginalPrice(),
                order.deliveryDiscountPrice(),
                order.adjustmentAmount(),
                order.totalAmount()),
            rows);
    }
}
