package com.lemnt.order.infrastructure.order.gateway;

import com.lemnt.member.client.MemberOrderBenefitsClient;
import com.lemnt.order.domain.aftersale.model.AfterSale;
import com.lemnt.order.domain.order.gateway.OrderBenefitsProgressGateway;
import com.lemnt.order.domain.order.gateway.OrderResourceGateway;
import com.lemnt.product.client.ProductCatalogClient;
import com.lemnt.product.client.bo.StockReservationQueryBo;
import com.lemnt.promotion.client.PromotionLockClient;

import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.LinkedHashMap;

/** 原事件成功消费的回执才代表权益完成，任一远端失败继续保留待处理。 */
@Component
@RequiredArgsConstructor
public class OrderBenefitsProgressGatewayImpl implements OrderBenefitsProgressGateway {
    private final OrderResourceGateway orders;

    @DubboReference(check = false, timeout = 5000, retries = 0)
    private ProductCatalogClient products;

    @DubboReference(check = false, timeout = 5000, retries = 0)
    private PromotionLockClient promotions;

    @DubboReference(check = false, timeout = 5000, retries = 0)
    private MemberOrderBenefitsClient members;

    public boolean paid(String no) {
        var plan = orders.plan(no);
        var quantities = new LinkedHashMap<Long, Integer>();
        plan.lines().forEach(l -> quantities.merge(l.skuId(), l.quantity(), Math::addExact));
        for (var line : quantities.entrySet()) {
            var r = products.reservationStatus(new StockReservationQueryBo(no, line.getKey()));
            if (r == null || !"CONFIRMED".equals(r.status()) || r.quantity() != line.getValue())
                return false;
        }
        var promotion = promotions.query(no);
        return promotion != null && "PAID".equals(promotion.status()) && members.paidApplied(no);
    }

    public boolean refunded(AfterSale af) {
        if (af.skuId() == null || af.quantity() == null || af.refundRuleVersion() == null)
            return false;
        var r = products.reservationStatus(new StockReservationQueryBo(af.refundNo(), af.skuId()));
        return r != null
                && "RETURNED".equals(r.status())
                && r.quantity() == af.quantity()
                && af.orderNo().equals(r.sourceNo())
                && promotions.refundApplied(af.refundNo())
                && members.refundApplied(af.refundNo());
    }
}
