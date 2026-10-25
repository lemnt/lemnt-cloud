package com.lemnt.trade.infrastructure.checkout.gateway;

import com.lemnt.trade.domain.checkout.gateway.CheckoutGateway;
import com.lemnt.trade.domain.checkout.model.*;
import com.lemnt.trade.infrastructure.checkout.converter.CheckoutConverter;
import com.lemnt.trade.infrastructure.checkout.dataobject.*;
import com.lemnt.trade.infrastructure.checkout.mapper.CheckoutMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import java.util.List;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class CheckoutGatewayImpl implements CheckoutGateway {
    private final CheckoutMapper mapper;

    @Override
    public LocalDateTime now() {
        return mapper.now();
    }

    @Override
    public void lockMember(Long memberId) {
        mapper.ensureMember(memberId);
        if (mapper.lockMember(memberId) == null) throw new IllegalStateException("会员结算锁失败");
    }

    @Override
    public boolean cartInCheckout(Long cartId) {
        return mapper.cartInCheckout(cartId) > 0;
    }

    @Override
    public List<CartLine> cart(Long memberId) {
        var values = mapper.cart(memberId);
        if (values.size() > 200) throw new IllegalStateException("购物车条目超过200");
        return values.stream().map(CheckoutConverter::cart).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CartLine saveCart(Long memberId, Long productId, int quantity, boolean selected) {
        lockMember(memberId);
        var row = mapper.cartItem(memberId, productId);
        if (row == null) {
            if (cart(memberId).size() >= 200) throw new IllegalArgumentException("购物车最多200项商品");
            row = new CartDO();
            row.setMemberId(memberId);
            row.setProductId(productId);
            row.setQuantity(quantity);
            row.setSelected(selected);
            mapper.insertCart(row);
        } else {
            row.setQuantity(quantity);
            row.setSelected(selected);
            if (mapper.updateCart(row) != 1) throw new IllegalStateException("购物车已变化");
        }
        return CheckoutConverter.cart(mapper.cartItem(memberId, productId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteCart(Long memberId, Long productId) {
        lockMember(memberId);
        return mapper.deleteCart(memberId, productId) == 1;
    }

    @Override
    public List<CouponTemplate> templates() {
        var values = mapper.templates();
        if (values.size() > 1000) throw new IllegalStateException("优惠券模板超过1000条，请接入分页");
        return values.stream().map(CheckoutConverter::template).toList();
    }

    @Override
    public CouponTemplate template(Long id) {
        return CheckoutConverter.template(mapper.template(id));
    }

    @Override
    public CouponTemplate lockTemplate(Long id) {
        return CheckoutConverter.template(mapper.lockTemplate(id));
    }

    @Override
    public CouponTemplate createTemplate(CouponTemplate value) {
        var row = new CouponTemplateDO();
        row.setCode(value.code());
        row.setName(value.name());
        row.setKind(value.kind());
        row.setThreshold(value.threshold());
        row.setFixedAmount(value.fixedAmount());
        row.setDiscountPercent(value.discountPercent());
        row.setDiscountLimit(value.discountLimit());
        row.setTotalCount(value.totalCount());
        row.setIssuedCount(0);
        row.setValidFrom(value.validFrom());
        row.setValidUntil(value.validUntil());
        row.setEnabled(true);
        mapper.insertTemplate(row);
        return CheckoutConverter.template(mapper.template(row.getId()));
    }

    @Override
    public boolean enableTemplate(Long id, boolean enabled) {
        return mapper.enableTemplate(id, enabled) == 1;
    }

    @Override
    public List<Coupon> coupons(Long memberId) {
        var values = mapper.coupons(memberId);
        if (values.size() > 1000) throw new IllegalStateException("会员优惠券超过1000条，请接入分页");
        return values.stream().map(CheckoutConverter::coupon).toList();
    }

    @Override
    public Coupon coupon(Long memberId, Long id, boolean forUpdate) {
        return CheckoutConverter.coupon(
                forUpdate ? mapper.lockCouponRow(memberId, id) : mapper.coupon(memberId, id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Coupon issue(Long memberId, Long templateId) {
        lockMember(memberId);
        var previous = mapper.issued(memberId, templateId);
        if (previous != null) return CheckoutConverter.coupon(previous);
        if (mapper.takeQuota(templateId) != 1) throw new IllegalStateException("优惠券未开放、已过期或名额已用完");
        var row = new CouponDO();
        row.setMemberId(memberId);
        row.setTemplateId(templateId);
        mapper.insertCoupon(row);
        return CheckoutConverter.coupon(mapper.coupon(memberId, row.getId()));
    }

    @Override
    public void lockCoupon(Long memberId, Long couponId, String orderNo) {
        if (mapper.lockCoupon(memberId, couponId, orderNo) != 1)
            throw new IllegalStateException("优惠券已被使用或锁定");
    }

    @Override
    public void useCoupon(String orderNo) {
        var record = mapper.receipt(orderNo);
        if (record == null || record.getCouponId() == null) return;
        var coupon = mapper.lockCouponRow(record.getMemberId(), record.getCouponId());
        if (coupon == null
                || !orderNo.equals(coupon.getOrderNo())
                || !java.util.Set.of("LOCKED", "USED").contains(coupon.getStatus())) {
            throw new IllegalStateException("订单优惠券锁定状态不一致");
        }
        if ("USED".equals(coupon.getStatus())) return;
        if (mapper.useCoupon(orderNo) != 1) throw new IllegalStateException("优惠券核销失败");
    }

    @Override
    public void releaseCoupon(String orderNo) {
        var record = mapper.receipt(orderNo);
        if (record == null || record.getCouponId() == null) return;
        var coupon = mapper.lockCouponRow(record.getMemberId(), record.getCouponId());
        if (coupon != null && "AVAILABLE".equals(coupon.getStatus()) && coupon.getOrderNo() == null)
            return;
        if (coupon == null
                || !orderNo.equals(coupon.getOrderNo())
                || !"LOCKED".equals(coupon.getStatus())) {
            throw new IllegalStateException("订单优惠券释放状态不一致");
        }
        if (mapper.releaseCoupon(orderNo) != 1) throw new IllegalStateException("优惠券释放失败");
    }

    @Override
    public CheckoutRecord findRequest(Long memberId, String requestNo) {
        var row = mapper.request(memberId, requestNo);
        return CheckoutConverter.checkout(
                row, row == null ? List.of() : mapper.references(row.getOrderNo()));
    }

    @Override
    public CheckoutRecord receipt(String orderNo) {
        return CheckoutConverter.checkout(mapper.receipt(orderNo), mapper.references(orderNo));
    }

    @Override
    public void save(CheckoutRecord value) {
        var row = new CheckoutDO();
        row.setOrderNo(value.orderNo());
        row.setMemberId(value.memberId());
        row.setRequestNo(value.requestNo());
        row.setFingerprint(value.fingerprint());
        row.setOriginalAmount(value.originalAmount());
        row.setDiscountAmount(value.discountAmount());
        row.setPayableAmount(value.payableAmount());
        row.setCouponId(value.couponId());
        mapper.insertCheckout(row);
        for (var reference : value.cartReferences()) {
            var child = new CheckoutCartDO();
            child.setOrderNo(value.orderNo());
            child.setCartId(reference.cartId());
            child.setCartVersion(reference.cartVersion());
            mapper.insertReference(child);
        }
    }

    @Override
    public void consumeCart(String orderNo, Long memberId) {
        for (var reference : mapper.references(orderNo))
            mapper.consumeCart(memberId, reference.getCartId(), reference.getCartVersion());
    }
}
