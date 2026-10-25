package com.lemnt.trade.domain.checkout.gateway;

import com.lemnt.trade.domain.checkout.model.*;
import java.time.LocalDateTime;
import java.util.List;

public interface CheckoutGateway {
    LocalDateTime now();

    void lockMember(Long memberId);

    List<CartLine> cart(Long memberId);

    boolean cartInCheckout(Long cartId);

    CartLine saveCart(Long memberId, Long productId, int quantity, boolean selected);

    boolean deleteCart(Long memberId, Long productId);

    List<CouponTemplate> templates();

    CouponTemplate template(Long id);

    CouponTemplate lockTemplate(Long id);

    CouponTemplate createTemplate(CouponTemplate template);

    boolean enableTemplate(Long id, boolean enabled);

    List<Coupon> coupons(Long memberId);

    Coupon coupon(Long memberId, Long id, boolean forUpdate);

    Coupon issue(Long memberId, Long templateId);

    void lockCoupon(Long memberId, Long couponId, String orderNo);

    void useCoupon(String orderNo);

    void releaseCoupon(String orderNo);

    CheckoutRecord findRequest(Long memberId, String requestNo);

    CheckoutRecord receipt(String orderNo);

    void save(CheckoutRecord record);

    void consumeCart(String orderNo, Long memberId);
}
