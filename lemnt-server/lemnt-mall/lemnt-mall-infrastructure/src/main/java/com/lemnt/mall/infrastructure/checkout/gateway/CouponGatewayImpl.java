package com.lemnt.mall.infrastructure.checkout.gateway;

import com.lemnt.mall.domain.checkout.gateway.CouponGateway;
import com.lemnt.mall.domain.checkout.model.CheckoutRecord;
import com.lemnt.mall.domain.checkout.model.Coupon;
import com.lemnt.mall.infrastructure.checkout.converter.CheckoutConverter;
import com.lemnt.mall.infrastructure.checkout.dataobject.CouponDO;
import com.lemnt.mall.infrastructure.checkout.mapper.CouponMapper;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Set;

/** Coupon 表持久化实现。 */
@Component
@RequiredArgsConstructor
public class CouponGatewayImpl implements CouponGateway {
    private final CouponMapper couponMapper;

    @Override
    public List<Coupon> coupons(Long memberId) {
        var values = couponMapper.coupons(memberId);
        if (values.size() > 1000) {
            throw new IllegalStateException("会员优惠券超过1000条，请接入分页");
        }
        return values.stream().map(CheckoutConverter::coupon).toList();
    }

    @Override
    public Coupon coupon(Long memberId, Long id, boolean forUpdate) {
        return CheckoutConverter.coupon(
                forUpdate ? couponMapper.lockCouponRow(memberId, id) : couponMapper.coupon(memberId, id));
    }

    @Override
    public void lockCoupon(Long memberId, Long couponId, String orderNo) {
        if (couponMapper.lockCoupon(memberId, couponId, orderNo) != 1) {
            throw new IllegalStateException("优惠券已被使用或锁定");
        }
    }

    @Override
    /**
     * 已签发。
     */
    public Coupon issued(Long m, Long t) {
        return CheckoutConverter.coupon(couponMapper.storefrontIssued(m, t));
    }

    @Override
    /**
     * 券包。
     */
    public List<Coupon> wallet(Long m, int o, int l) {
        return couponMapper.wallet(m, o, l).stream().map(CheckoutConverter::coupon).toList();
    }

    @Override
    /**
     * 券包数量。
     */
    public long walletCount(Long m) {
        return couponMapper.walletCount(m);
    }

    /** 创建会员优惠券。 */
    @Override
    public Coupon insertCoupon(Long memberId, Long templateId) {
        var row = new CouponDO();
        row.setMemberId(memberId);
        row.setTemplateId(templateId);
        couponMapper.insertCoupon(row);
        return CheckoutConverter.coupon(couponMapper.coupon(memberId, row.getId()));
    }

    /** 按已读取的结算记录维护优惠券状态。 */
    @Override
    public void useCoupon(CheckoutRecord record) {
        String orderNo = record.orderNo();
        if (record == null || record.couponId() == null) {
            return ;
        }
        var coupon = couponMapper.lockCouponRow(record.memberId(), record.couponId());
        if (coupon == null
                || !orderNo.equals(coupon.getOrderNo())
                || !Set.of("LOCKED", "USED").contains(coupon.getStatus())) {
            throw new IllegalStateException("订单优惠券锁定状态不一致");
        }
        if ("USED".equals(coupon.getStatus())) {
            return ;
        }
        if (couponMapper.useCoupon(orderNo) != 1) {
            throw new IllegalStateException("优惠券核销失败");
        }
    }

    /** 按已读取的结算记录维护优惠券状态。 */
    @Override
    public void releaseCoupon(CheckoutRecord record) {
        String orderNo = record.orderNo();
        if (record == null || record.couponId() == null) {
            return ;
        }
        var coupon = couponMapper.lockCouponRow(record.memberId(), record.couponId());
        if (coupon != null && "AVAILABLE".equals(coupon.getStatus()) && coupon.getOrderNo() == null) {
            return ;
        }
        if (coupon == null
                || !orderNo.equals(coupon.getOrderNo())
                || !"LOCKED".equals(coupon.getStatus())) {
            throw new IllegalStateException("订单优惠券释放状态不一致");
        }
        if (couponMapper.releaseCoupon(orderNo) != 1) {
            throw new IllegalStateException("优惠券释放失败");
        }
    }
}
