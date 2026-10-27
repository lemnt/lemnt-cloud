package com.lemnt.mall.application.checkout.service;

import com.lemnt.mall.domain.checkout.gateway.CartGateway;
import com.lemnt.mall.domain.checkout.gateway.CheckoutAccountGateway;
import com.lemnt.mall.domain.checkout.gateway.CheckoutCartGateway;
import com.lemnt.mall.domain.checkout.gateway.CheckoutGateway;
import com.lemnt.mall.domain.checkout.gateway.CouponGateway;
import com.lemnt.mall.domain.checkout.gateway.CouponTemplateGateway;
import com.lemnt.mall.domain.checkout.model.CartLine;
import com.lemnt.mall.domain.checkout.model.CheckoutRecord;
import com.lemnt.mall.domain.checkout.model.Coupon;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/** CheckoutService 跨表应用编排，保持原有事务和锁顺序。 */
@Component
@RequiredArgsConstructor
public class CheckoutService {
    private final CheckoutAccountGateway checkoutAccountGateway;
    private final CartGateway cartGateway;
    private final CouponTemplateGateway couponTemplateGateway;
    private final CouponGateway couponGateway;
    private final CheckoutGateway checkoutGateway;
    private final CheckoutCartGateway checkoutCartGateway;

    /** 在会员结算锁内保存购物车。 */
    @Transactional(rollbackFor = Exception.class)
    public CartLine saveCart(Long memberId, Long productId, int quantity, boolean selected) {
        checkoutAccountGateway.lockMember(memberId);
        return cartGateway.saveCart(memberId, productId, quantity, selected);
    }

    /** 在会员结算锁内删除购物车。 */
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteCart(Long memberId, Long productId) {
        checkoutAccountGateway.lockMember(memberId);
        return cartGateway.deleteCart(memberId, productId);
    }

    /** 幂等发券，锁会员、检查原券、占用模板名额后写入券。 */
    @Transactional(rollbackFor = Exception.class)
    public Coupon issue(Long memberId, Long templateId) {
        checkoutAccountGateway.lockMember(memberId);
        var previous = couponGateway.issued(memberId, templateId);
        if (previous != null) {
            return previous;
        }
        if (!couponTemplateGateway.takeQuota(templateId)) {
            throw new IllegalStateException("优惠券未开放、已过期或名额已用完");
        }
        return couponGateway.insertCoupon(memberId, templateId);
    }

    /** 按结算主体核销锁定券。 */
    public void useCoupon(String orderNo) {
        var record = checkoutGateway.receipt(orderNo);
        if (record != null) {
            couponGateway.useCoupon(record);
        }
    }

    /** 按结算主体释放锁定券。 */
    public void releaseCoupon(String orderNo) {
        var record = checkoutGateway.receipt(orderNo);
        if (record != null) {
            couponGateway.releaseCoupon(record);
        }
    }

    /** 组合提单请求与购物车版本引用。 */
    public CheckoutRecord findRequest(Long memberId, String requestNo) {
        return withReferences(checkoutGateway.findRequest(memberId, requestNo));
    }

    /** 组合结算回执与购物车版本引用。 */
    public CheckoutRecord receipt(String orderNo) {
        return withReferences(checkoutGateway.receipt(orderNo));
    }

    /** 保存结算主体及其购物车版本引用，由调用方事务覆盖。 */
    public void save(CheckoutRecord value) {
        checkoutGateway.save(value);
        checkoutCartGateway.saveReferences(value.orderNo(), value.cartReferences());
    }

    /** 消费结算时占用的精确购物车版本。 */
    public void consumeCart(String orderNo, Long memberId) {
        for (var reference : checkoutCartGateway.references(orderNo)) {
            cartGateway.consume(memberId, reference);
        }
    }

    private CheckoutRecord withReferences(CheckoutRecord value) {
        return value == null ? null : new CheckoutRecord(value.orderNo(),
                value.memberId(),
                value.requestNo(),
                value.fingerprint(),
                value.originalAmount(),
                value.discountAmount(),
                value.payableAmount(),
                value.couponId(),
                checkoutCartGateway.references(value.orderNo()));
    }
}
