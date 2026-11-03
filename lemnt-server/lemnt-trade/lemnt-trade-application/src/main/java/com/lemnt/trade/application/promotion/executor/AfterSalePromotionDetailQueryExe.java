package com.lemnt.trade.application.promotion.executor;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.trade.application.promotion.dto.AfterSalePromotionDetailDTO;
import com.lemnt.trade.domain.aftersale.gateway.AfterSaleGateway;
import com.lemnt.trade.domain.order.gateway.TradeOrderGateway;
import com.lemnt.trade.domain.order.model.OrderRules;
import com.lemnt.trade.domain.payment.gateway.PayNotifyTaskGateway;
import com.lemnt.trade.domain.paymentrefund.gateway.PaymentRefundGateway;
import com.lemnt.trade.domain.promotion.gateway.OrderPromotionGateway;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/** 查询退单生成时冻结的方案，不能把计划返积分展示成已到账。 */
@Component
@RequiredArgsConstructor
public class AfterSalePromotionDetailQueryExe {
    private final AfterSaleGateway afterSales;
    private final TradeOrderGateway orders;
    private final OrderPromotionGateway promotions;
    private final PayNotifyTaskGateway tasks;
    private final PaymentRefundGateway refunds;

    public AfterSalePromotionDetailDTO execute(String no) {
        return read(null, no);
    }

    public AfterSalePromotionDetailDTO execute(Long memberId, String no) {
        if (memberId == null || memberId <= 0) {
            throw new ServiceException("售后单不存在", 400);
        }
        return read(memberId, no);
    }

    private AfterSalePromotionDetailDTO read(Long memberId, String no) {
        var afterSale = afterSales.afterSale(OrderRules.text(no, 40));
        if (afterSale == null || (memberId != null && !memberId.equals(afterSale.memberId()))) {
            throw new ServiceException("售后单不存在", 400);
        }
        var order = orders.findHeader(afterSale.orderNo());
        if (order == null || !order.memberId().equals(afterSale.memberId())) {
            throw new ServiceException("售后单不存在", 400);
        }
        var source = promotions.find(order.id());
        var result = new ArrayList<AfterSalePromotionDetailDTO.Promotion>();
        for (var row : promotions.refunds(List.of(afterSale.id()))) {
            var header =
                    source.stream()
                            .filter(
                                    p ->
                                            p.items().stream()
                                                    .anyMatch(
                                                            i ->
                                                                    i.id().equals(
                                                                                    row
                                                                                            .orderItemPromotionId())))
                            .findFirst()
                            .orElseThrow(() -> new IllegalStateException("退款优惠来源不一致"));
            result.add(
                    new AfterSalePromotionDetailDTO.Promotion(
                            header.promotionType(),
                            header.promotionName(),
                            row.refundDiscountAmount(),
                            row.refundDeliveryDiscountPrice(),
                            row.returnUsePoint(),
                            row.deductGivePoint()));
        }
        String status = "NOT_STARTED";
        if ("REFUNDED".equals(afterSale.status())) {
            var task = tasks.findByBiz("AFTER_SALE", afterSale.refundNo());
            if (task == null) {
                var refund = refunds.findByAfterSaleNo(afterSale.refundNo());
                if (refund != null) {
                    task = tasks.findByBiz("REFUND", refund.refundNo());
                }
            }
            status = task == null ? "UNKNOWN" : task.status();
        }
        boolean complete =
                afterSale.refundDiscountAmount() != null
                        && afterSale.returnUsePoint() != null
                        && afterSale.deductGivePoint() != null;
        return new AfterSalePromotionDetailDTO(
                afterSale.refundNo(),
                afterSale.orderItemId(),
                afterSale.quantity(),
                afterSale.amount(),
                afterSale.refundDiscountAmount(),
                afterSale.refundDeliveryDiscountPrice(),
                afterSale.returnUsePoint(),
                afterSale.deductGivePoint(),
                afterSale.refundRuleVersion(),
                complete ? "COMPLETE" : "LEGACY_PARTIAL",
                status,
                result);
    }
}
