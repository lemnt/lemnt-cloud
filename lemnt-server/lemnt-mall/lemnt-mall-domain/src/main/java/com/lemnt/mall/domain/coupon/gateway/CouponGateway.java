package com.lemnt.mall.domain.coupon.gateway;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.mall.domain.coupon.model.Coupon;
import com.lemnt.mall.domain.coupon.model.CouponFilter;
import com.lemnt.mall.domain.coupon.model.CouponOrderReference;

import java.time.LocalDateTime;
import java.util.List;

/** Coupon 表领域存储接口。 */
public interface CouponGateway {
    /** 按会员、模板和持券状态分页，不截断后台记录。 */
    PageResult<Coupon> page(CouponFilter filter);

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
    Coupon insertCoupon(Coupon value);

    /** 读取会员当前模板已领总数，含已用与过期，不含已撤回。 */
    long takeCount(Long memberId, Long templateId);

    /** 逻辑撤回未用/已过期券，返回是否实际更新。 */
    boolean revoke(Long memberId, Long id);

    /** 批量过期未使用券，不触碰已锁定订单。 */
    int expire(LocalDateTime now);

    /** 根据优惠券订单归属维护状态。 */
    boolean useCoupon(CouponOrderReference reference);

    /** 根据优惠券订单归属维护状态。 */
    void releaseCoupon(CouponOrderReference reference);
    /** 按订单读取已绑定券，支付回调不依赖结算缓存有效期。 */
    Coupon byOrderNo(String orderNo);
    /** 整单退款后归还已核销券，重复调用不产生新券。 */
    boolean returnUsedCoupon(String orderNo);
}
