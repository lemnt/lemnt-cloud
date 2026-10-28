package com.lemnt.mall.infrastructure.coupon.gateway;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.mall.domain.coupon.gateway.CouponGateway;
import com.lemnt.mall.domain.coupon.model.Coupon;
import com.lemnt.mall.domain.coupon.model.CouponFilter;
import com.lemnt.mall.domain.coupon.model.CouponOrderReference;
import com.lemnt.mall.infrastructure.coupon.converter.CouponConverter;
import com.lemnt.mall.infrastructure.coupon.dataobject.CouponDO;
import com.lemnt.mall.infrastructure.coupon.mapper.CouponMapper;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/** Coupon 表持久化实现。 */
@Component
@RequiredArgsConstructor
public class CouponGatewayImpl implements CouponGateway {
    private final CouponMapper couponMapper;

    /** 简单分页条件使用 MyBatis-Plus Lambda。 */
    @Override
    public PageResult<Coupon> page(CouponFilter filter) {
        var page = couponMapper.selectPage(new Page<>(filter.pageNum(), filter.pageSize()),
                Wrappers.<CouponDO>lambdaQuery().eq(filter.memberId() != null, CouponDO::getMemberId, filter.memberId())
                        .eq(filter.templateId() != null, CouponDO::getTemplateId, filter.templateId())
                        .eq(filter.status() != null, CouponDO::getStatus, filter.status()).orderByDesc(CouponDO::getId));
        return PageResult.of(page.getRecords().stream().map(CouponConverter::coupon).toList(), page.getTotal());
    }

    @Override
    public List<Coupon> coupons(Long memberId) {
        var values = couponMapper.selectList(Wrappers.<CouponDO>lambdaQuery()
                .eq(CouponDO::getMemberId, memberId)
                .orderByDesc(CouponDO::getId)
                .last("LIMIT 1001"));
        if (values.size() > 1000) {
            throw new IllegalStateException("会员优惠券超过1000条，请接入分页");
        }
        return values.stream().map(CouponConverter::coupon).toList();
    }

    @Override
    public Coupon coupon(Long memberId, Long id, boolean forUpdate) {
        return CouponConverter.coupon(
                forUpdate ? couponMapper.lockCouponRow(memberId,
                    id) : couponMapper.selectOne(Wrappers.<CouponDO>lambdaQuery()
                .eq(CouponDO::getMemberId, memberId)
                .eq(CouponDO::getId, id)));
    }

    @Override
    public void lockCoupon(Long memberId, Long couponId, String orderNo) {
        if (couponMapper.update(null, Wrappers.<CouponDO>lambdaUpdate()
                .set(CouponDO::getStatus, "LOCKED")
                .set(CouponDO::getOrderNo, orderNo)
                .eq(CouponDO::getId, couponId)
                .eq(CouponDO::getMemberId, memberId)
                .eq(CouponDO::getStatus, "AVAILABLE")) != 1) {
            throw new IllegalStateException("优惠券已被使用或锁定");
        }
    }

    @Override
    /**
     * 已签发。
     */
    public Coupon issued(Long m, Long t) {
        return CouponConverter.coupon(couponMapper.selectOne(Wrappers.<CouponDO>lambdaQuery()
                .eq(CouponDO::getMemberId, m)
                .eq(CouponDO::getTemplateId, t).orderByDesc(CouponDO::getId).last("LIMIT 1")));
    }

    @Override
    /**
     * 券包。
     */
    public List<Coupon> wallet(Long m, int o, int l) {
        return couponMapper.selectPage(new Page<>((long) o / l + 1, l, false),
                Wrappers.<CouponDO>lambdaQuery()
                .eq(CouponDO::getMemberId, m)
                .orderByDesc(CouponDO::getId)).getRecords().stream().map(CouponConverter::coupon).toList();
    }

    @Override
    /**
     * 券包数量。
     */
    public long walletCount(Long m) {
        return couponMapper.selectCount(Wrappers.<CouponDO>lambdaQuery()
                .eq(CouponDO::getMemberId, m));
    }

    /** 创建会员优惠券。 */
    @Override
    public Coupon insertCoupon(Coupon value) {
        var row = CouponConverter.coupon(value);
        couponMapper.insert(row);
        return CouponConverter.coupon(couponMapper.selectById(row.getId()));
    }

    /** 已领取数量包含已用、过期券，不包含已撤回券。 */
    @Override
    public long takeCount(Long memberId, Long templateId) {
        return couponMapper.selectCount(Wrappers.<CouponDO>lambdaQuery()
                .eq(CouponDO::getMemberId, memberId).eq(CouponDO::getTemplateId, templateId));
    }

    /** 仅撤回未使用或已过期的会员券。 */
    @Override
    public boolean revoke(Long memberId, Long id) {
        return couponMapper.delete(Wrappers.<CouponDO>lambdaQuery()
                .eq(CouponDO::getMemberId, memberId).eq(CouponDO::getId, id)
                .in(CouponDO::getStatus, "AVAILABLE", "EXPIRED")) == 1;
    }

    /** 已锁定的券随订单释放，不能由过期任务解除锁定。 */
    @Override
    public int expire(LocalDateTime now) {
        return couponMapper.update(null, Wrappers.<CouponDO>lambdaUpdate()
                .set(CouponDO::getStatus, "EXPIRED").eq(CouponDO::getStatus, "AVAILABLE")
                .le(CouponDO::getValidUntil, now));
    }

    /** 根据优惠券订单归属维护状态。 */
    @Override
    public boolean useCoupon(CouponOrderReference reference) {
        if (reference == null || reference.couponId() == null) {
            return false;
        }
        String orderNo = reference.orderNo();
        var coupon = couponMapper.lockCouponRow(reference.memberId(), reference.couponId());
        if (coupon == null
                || !orderNo.equals(coupon.getOrderNo())
                || !Set.of("LOCKED", "USED").contains(coupon.getStatus())) {
            throw new IllegalStateException("订单优惠券锁定状态不一致");
        }
        if ("USED".equals(coupon.getStatus())) {
            return false;
        }
        if (couponMapper.update(null, Wrappers.<CouponDO>lambdaUpdate()
                .set(CouponDO::getStatus, "USED")
                .set(CouponDO::getUseTime, couponMapper.now())
                .eq(CouponDO::getOrderNo, orderNo)
                .eq(CouponDO::getStatus, "LOCKED")) != 1) {
            throw new IllegalStateException("优惠券核销失败");
        }
        return true;
    }

    /** 根据优惠券订单归属维护状态。 */
    @Override
    public void releaseCoupon(CouponOrderReference reference) {
        if (reference == null || reference.couponId() == null) {
            return;
        }
        String orderNo = reference.orderNo();
        var coupon = couponMapper.lockCouponRow(reference.memberId(), reference.couponId());
        if (coupon != null && Set.of("AVAILABLE", "EXPIRED").contains(coupon.getStatus()) && coupon.getOrderNo() == null) {
            return ;
        }
        if (coupon == null
                || !orderNo.equals(coupon.getOrderNo())
                || !"LOCKED".equals(coupon.getStatus())) {
            throw new IllegalStateException("订单优惠券释放状态不一致");
        }
        if (couponMapper.update(null, Wrappers.<CouponDO>lambdaUpdate()
                .set(CouponDO::getStatus, returnedStatus(coupon.getValidUntil()))
                .set(CouponDO::getOrderNo, null)
                .eq(CouponDO::getOrderNo, orderNo)
                .eq(CouponDO::getStatus, "LOCKED")) != 1) {
            throw new IllegalStateException("优惠券释放失败");
        }
    }
    /** 读取订单绑定的优惠券事实。 */
    @Override
    public Coupon byOrderNo(String orderNo) {
        return CouponConverter.coupon(couponMapper.selectOne(Wrappers.<CouponDO>lambdaQuery()
                .eq(CouponDO::getOrderNo, orderNo)));
    }
    @Override
    public boolean returnUsedCoupon(String orderNo) {
        var coupon = byOrderNo(orderNo);
        if (coupon == null) {
            return false;
        }
        var current = couponMapper.lockCouponRow(coupon.memberId(), coupon.id());
        if (current == null || !orderNo.equals(current.getOrderNo()) || !"USED".equals(current.getStatus())) {
            throw new IllegalStateException("退款优惠券与原核销订单不一致");
        }
        if (couponMapper.update(null, Wrappers.<CouponDO>lambdaUpdate()
                .set(CouponDO::getStatus, returnedStatus(current.getValidUntil()))
                .set(CouponDO::getUseTime, null).set(CouponDO::getOrderNo, null)
                .eq(CouponDO::getId, coupon.id()).eq(CouponDO::getOrderNo, orderNo)
                .eq(CouponDO::getStatus, "USED")) != 1) {
            throw new IllegalStateException("退款退券失败");
        }
        return true;
    }
    /** 释放或退款时按原券到期日恢复状态。 */
    private String returnedStatus(LocalDateTime validUntil) {
        return validUntil != null && !validUntil.isAfter(couponMapper.now()) ? "EXPIRED" : "AVAILABLE";
    }
}
