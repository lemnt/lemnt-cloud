package com.lemnt.mall.application.settlement.service;

import com.lemnt.mall.domain.coupon.gateway.CouponTemplateGateway;
import java.util.Set;

import com.lemnt.mall.domain.cart.gateway.CartGateway;
import com.lemnt.mall.domain.coupon.gateway.CouponGateway;
import com.lemnt.mall.domain.coupon.model.CouponOrderReference;
import com.lemnt.mall.domain.settlement.gateway.SettlementGateway;
import com.lemnt.mall.domain.settlement.model.SettlementOrder;
import com.lemnt.mall.domain.settlement.model.SettlementRecord;
import com.lemnt.mall.domain.settlement.model.SettlementSubmission;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/** 结算回执、购物车版本引用和订单券状态编排。 */
@Component
@RequiredArgsConstructor
public class SettlementRecordService {
    private final CartGateway cartGateway;
    private final CouponGateway couponGateway;
    private final CouponTemplateGateway couponTemplateGateway;
    private final SettlementGateway settlementGateway;

    /** 同步校验订单优惠券归属及锁定状态。 */
    @Transactional(rollbackFor = Exception.class)
    public void verifyCoupon(String orderNo) {
        var record = receipt(orderNo);
        if (record == null) {
            throw new IllegalStateException("下单回执不存在");
        }
        if (record.couponId() == null) {
            return;
        }
        var coupon = couponGateway.coupon(record.memberId(), record.couponId(), true);
        if (coupon == null || !record.memberId().equals(coupon.memberId())
                || !orderNo.equals(coupon.orderNo())
                || !Set.of("LOCKED", "USED").contains(coupon.status())) {
            throw new IllegalStateException("订单优惠券归属或锁定状态不一致");
        }
    }

    /** 按结算主体核销锁定券。 */
    @Transactional(rollbackFor = Exception.class)
    public void useCoupon(String orderNo) {
        var coupon = couponGateway.byOrderNo(orderNo);
        if (coupon == null) {
            var record = receipt(orderNo);
            if (record != null && record.couponId() != null) {
                throw new IllegalStateException("订单优惠券锁定状态不一致");
            }
            return;
        }
        if (couponGateway.useCoupon(new CouponOrderReference(coupon.memberId(), coupon.id(), orderNo))) {
            couponTemplateGateway.changeCounts(coupon.templateId(), 0, 1);
        }
    }

    /** 按结算主体释放锁定券。 */
    @Transactional(rollbackFor = Exception.class)
    public void releaseCoupon(String orderNo) {
        var coupon = couponGateway.byOrderNo(orderNo);
        if (coupon != null) {
            couponGateway.releaseCoupon(new CouponOrderReference(coupon.memberId(), coupon.id(), orderNo));
        }
    }

    /** 组合提单请求与购物车版本引用。 */
    public SettlementRecord findRequest(Long memberId, String requestNo) {
        return settlementGateway.findRequest(memberId, requestNo);
    }

    /** 组合结算回执与购物车版本引用。 */
    public SettlementRecord receipt(String orderNo) {
        var value = settlementGateway.submission(orderNo);
        return value == null ? null : value.record();
    }

    /** 将原始订单快照与重试上下文一并缓存。 */
    public void save(SettlementRecord value, SettlementOrder order) {
        settlementGateway.saveSubmission(new SettlementSubmission(order, value));
    }

    /** 消费结算时占用的精确购物车版本。 */
    public void consumeCart(String orderNo, Long memberId) {
        var value = receipt(orderNo);
        if (value == null) {
            throw new IllegalStateException("结算提交缓存已失效，请查询原订单");
        }
        for (var reference : value.cartReferences()) {
            cartGateway.consume(memberId, reference);
        }
    }

    /** 仅由已确认整单退款的生命周期处理调用。 */
    @Transactional(rollbackFor = Exception.class)
    public void returnUsedCoupon(String orderNo) {
        var coupon = couponGateway.byOrderNo(orderNo);
        if (coupon != null && couponGateway.returnUsedCoupon(orderNo)) {
            couponTemplateGateway.changeCounts(coupon.templateId(), 0, -1);
        }
    }
}
