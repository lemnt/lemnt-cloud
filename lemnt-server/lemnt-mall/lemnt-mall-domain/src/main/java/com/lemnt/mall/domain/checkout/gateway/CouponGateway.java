package com.lemnt.mall.domain.checkout.gateway;

import com.lemnt.mall.domain.checkout.model.CheckoutRecord;
import com.lemnt.mall.domain.checkout.model.Coupon;

import java.util.List;

/** Coupon 表领域存储接口。 */
public interface CouponGateway {
    /**
     * 查询优惠券。
     */
    List<Coupon> coupons(Long memberId);

    /**
     * 优惠券。
     */
    Coupon coupon(Long memberId, Long id, boolean forUpdate);

    /**
     * 锁定优惠券。
     */
    void lockCoupon(Long memberId, Long couponId, String orderNo);

    /**
     * 已签发。
     */
    Coupon issued(Long memberId, Long templateId);

    /**
     * 券包。
     */
    List<Coupon> wallet(Long memberId, int offset, int limit);

    /**
     * 券包数量。
     */
    long walletCount(Long memberId);

    /** 创建会员优惠券。 */
    Coupon insertCoupon(Long memberId, Long templateId);

    /** 按已读取的结算记录维护优惠券状态。 */
    void useCoupon(CheckoutRecord record);

    /** 按已读取的结算记录维护优惠券状态。 */
    void releaseCoupon(CheckoutRecord record);
}
