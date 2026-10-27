package com.lemnt.mall.application.store.service;

import com.lemnt.mall.application.checkout.converter.CheckoutDTOConverter;
import com.lemnt.mall.application.checkout.dto.CouponDTO;
import com.lemnt.mall.application.checkout.service.CheckoutMemberService;
import com.lemnt.mall.application.checkout.service.CheckoutService;
import com.lemnt.mall.application.store.cmd.CartAddCmd;
import com.lemnt.mall.application.store.cmd.CouponClaimCmd;
import com.lemnt.mall.application.store.cmd.CouponPublicClaimCmd;
import com.lemnt.mall.application.store.dto.CartAdditionDTO;
import com.lemnt.mall.application.store.dto.CouponOfferDTO;
import com.lemnt.mall.application.store.dto.PromotionPageDTO;
import com.lemnt.mall.application.store.qry.PromotionPageQry;
import com.lemnt.mall.domain.catalog.gateway.ProductCatalogGateway;
import com.lemnt.mall.domain.checkout.gateway.CartGateway;
import com.lemnt.mall.domain.checkout.gateway.CheckoutAccountGateway;
import com.lemnt.mall.domain.checkout.gateway.CouponGateway;
import com.lemnt.mall.domain.checkout.gateway.CouponTemplateGateway;
import com.lemnt.mall.domain.checkout.model.CheckoutRules;
import com.lemnt.mall.domain.checkout.model.Coupon;
import com.lemnt.mall.domain.checkout.model.CouponTemplate;
import com.lemnt.mall.domain.store.gateway.CartAddRequestGateway;
import com.lemnt.mall.domain.store.gateway.CouponPublicClaimGateway;
import com.lemnt.mall.domain.store.model.CartAddition;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * 商城服务。
 */
@Component
@RequiredArgsConstructor
public class StorefrontService {
    private final CartAddRequestGateway cartAddRequestGateway;
    private final CartGateway cartGateway;
    private final CheckoutAccountGateway checkoutAccountGateway;
    private final CheckoutService checkoutService;
    private final CouponGateway couponGateway;
    private final CouponPublicClaimGateway couponPublicClaimGateway;
    private final CouponTemplateGateway couponTemplateGateway;
    private final CheckoutMemberService checkoutMemberService;
    private final ProductCatalogGateway productCatalogGateway;
    private void page(PromotionPageQry q) {
        if (q == null
                || q.pageNum() < 1
                || q.pageNum() > 1000000
                || q.pageSize() < 1
                || q.pageSize() > 100) {
            throw new IllegalArgumentException("分页参数不合法");
        }
        checkoutMemberService.requireActive(q.memberId());
    }

    private boolean valid(CouponTemplate t) {
        var now = checkoutAccountGateway.now();
        return t != null
                && t.enabled()
                && !t.validFrom().isAfter(now)
                && t.validUntil().isAfter(now);
    }

    private CouponOfferDTO offer(CouponTemplate t, Coupon c, boolean publicClaim) {
        return new CouponOfferDTO(
                CheckoutDTOConverter.template(t),
                c == null ? null : c.id(),
                c == null ? null : c.status(),
                c != null && "AVAILABLE".equals(c.status()) && valid(t),
                publicClaim && c == null && valid(t) && t.issuedCount() < t.totalCount());
    }

    /**
     * 可领取优惠。
     */
    @Transactional(readOnly = true)
    public PromotionPageDTO offers(PromotionPageQry q) {
        page(q);
        return new PromotionPageDTO(
                couponTemplateGateway.offers((q.pageNum() - 1) * q.pageSize(), q.pageSize()).stream()
                .map(t -> offer(t, couponGateway.issued(q.memberId(), t.id()), true))
                .toList(),
                couponTemplateGateway.offerCount());
    }

    /**
     * 券包。
     */
    @Transactional(readOnly = true)
    public PromotionPageDTO wallet(PromotionPageQry q) {
        page(q);
        return new PromotionPageDTO(
                couponGateway
                .wallet(q.memberId(), (q.pageNum() - 1) * q.pageSize(), q.pageSize())
                .stream()
                .map(
                c -> {
                var t = couponTemplateGateway.template(c.templateId());
                if (t == null) {
                throw new IllegalStateException("优惠券规则缺失，请联系服务方");
                }
                return offer(t, c, false);
                })
                .toList(),
                couponGateway.walletCount(q.memberId()));
    }

    /**
     * 公开编号集合。
     */
    public List<Long> publicIds() {
        return couponPublicClaimGateway.publicTemplateIds();
    }

    /**
     * 公开领取。
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean publicClaim(CouponPublicClaimCmd c) {
        CheckoutRules.id(c.templateId());
        if (couponTemplateGateway.lockTemplate(c.templateId()) == null) {
            throw new IllegalArgumentException("模板不存在");
        }
        couponPublicClaimGateway.claimable(c.templateId(), c.enabled());
        return true;
    }

    /**
     * 领取。
     */
    @Transactional(rollbackFor = Exception.class)
    public CouponDTO claim(CouponClaimCmd c) {
        checkoutMemberService.requireActive(c.memberId());
        CheckoutRules.id(c.templateId());
        checkoutAccountGateway.lockMember(c.memberId());
        var t = couponTemplateGateway.lockTemplate(c.templateId());
        var previous = couponGateway.issued(c.memberId(), c.templateId());
        if (previous != null) {
            return CheckoutDTOConverter.coupon(previous);
        }
        if (!couponPublicClaimGateway.claimable(c.templateId()) || !valid(t) || t.issuedCount() >= t.totalCount()) {
            throw new IllegalStateException("优惠券当前不可领取");
        }
        return CheckoutDTOConverter.coupon(checkoutService.issue(c.memberId(), c.templateId()));
    }

    /**
     * 新增。
     */
    @Transactional(rollbackFor = Exception.class)
    public CartAdditionDTO add(CartAddCmd c) {
        checkoutMemberService.requireActive(c.memberId());
        CheckoutRules.id(c.productId());
        String key = CheckoutRules.text(c.requestNo(), 64);
        if (c.quantity() < 1 || c.quantity() > 999) {
            throw new IllegalArgumentException("加购数量必须为1至999");
        }
        checkoutAccountGateway.lockMember(c.memberId());
        var old = cartAddRequestGateway.addition(c.memberId(), key);
        if (old != null) {
            if (!old.productId().equals(c.productId()) || old.quantity() != c.quantity()) {
                throw new IllegalStateException("请求编号已用于不同的加购内容");
            }
            return new CartAdditionDTO(key, c.productId(), old.quantity(), old.result().quantity());
        }
        var product = productCatalogGateway.findOnShelf(c.productId());
        if (product == null
                || product.stock() == null
                || product.price() == null
                || product.price().signum() <= 0) {
            throw new IllegalArgumentException("商品不存在或已下架");
        }
        var line =
                cartGateway.cart(c.memberId()).stream()
                .filter(v -> v.productId().equals(c.productId()))
                .findFirst()
                .orElse(null);
        int quantity = Math.addExact(line == null ? 0 : line.quantity(), c.quantity());
        if (quantity > 999 || quantity > product.stock()) {
            throw new IllegalStateException("数量超过当前库存或购物车上限");
        }
        var result = checkoutService.saveCart(c.memberId(), c.productId(), quantity, true);
        cartAddRequestGateway.addition(
                new CartAddition(c.memberId(), key, c.productId(), c.quantity(), result));
        return new CartAdditionDTO(key, c.productId(), c.quantity(), quantity);
    }
}
