package com.lemnt.trade.application.store.service;

import com.lemnt.trade.domain.store.gateway.StorefrontGateway;
import com.lemnt.trade.domain.store.model.CartAddition;
import com.lemnt.trade.domain.checkout.gateway.CheckoutGateway;
import com.lemnt.trade.domain.checkout.model.*;
import com.lemnt.trade.domain.gateway.ProductCatalogGateway;
import com.lemnt.trade.application.checkout.service.CheckoutMemberService;
import com.lemnt.trade.application.store.cmd.*;
import com.lemnt.trade.application.store.qry.PromotionPageQry;
import com.lemnt.trade.application.store.dto.*;
import com.lemnt.trade.application.checkout.dto.CouponDTO;
import com.lemnt.trade.application.checkout.converter.CheckoutDTOConverter;
import java.util.List;

@org.springframework.stereotype.Component
@lombok.RequiredArgsConstructor
public class StorefrontService {
    private final StorefrontGateway storefront;
    private final CheckoutGateway checkout;
    private final CheckoutMemberService members;
    private final ProductCatalogGateway products;

    private void page(PromotionPageQry q) {
        if (q == null
                || q.pageNum() < 1
                || q.pageNum() > 1000000
                || q.pageSize() < 1
                || q.pageSize() > 100) throw new IllegalArgumentException("分页参数不合法");
        members.requireActive(q.memberId());
    }

    private boolean valid(CouponTemplate t) {
        var now = checkout.now();
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

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public PromotionPageDTO offers(PromotionPageQry q) {
        page(q);
        return new PromotionPageDTO(
                storefront.offers((q.pageNum() - 1) * q.pageSize(), q.pageSize()).stream()
                        .map(t -> offer(t, storefront.issued(q.memberId(), t.id()), true))
                        .toList(),
                storefront.offerCount());
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public PromotionPageDTO wallet(PromotionPageQry q) {
        page(q);
        return new PromotionPageDTO(
                storefront
                        .wallet(q.memberId(), (q.pageNum() - 1) * q.pageSize(), q.pageSize())
                        .stream()
                        .map(
                                c -> {
                                    var t = checkout.template(c.templateId());
                                    if (t == null)
                                        throw new IllegalStateException("优惠券规则缺失，请联系服务方");
                                    return offer(t, c, false);
                                })
                        .toList(),
                storefront.walletCount(q.memberId()));
    }

    public List<Long> publicIds() {
        return storefront.publicTemplateIds();
    }

    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public boolean publicClaim(CouponPublicClaimCmd c) {
        CheckoutRules.id(c.templateId());
        if (checkout.lockTemplate(c.templateId()) == null)
            throw new IllegalArgumentException("模板不存在");
        storefront.claimable(c.templateId(), c.enabled());
        return true;
    }

    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public CouponDTO claim(CouponClaimCmd c) {
        members.requireActive(c.memberId());
        CheckoutRules.id(c.templateId());
        checkout.lockMember(c.memberId());
        var t = checkout.lockTemplate(c.templateId());
        var previous = storefront.issued(c.memberId(), c.templateId());
        if (previous != null) return CheckoutDTOConverter.coupon(previous);
        if (!storefront.claimable(c.templateId()) || !valid(t) || t.issuedCount() >= t.totalCount())
            throw new IllegalStateException("优惠券当前不可领取");
        return CheckoutDTOConverter.coupon(checkout.issue(c.memberId(), c.templateId()));
    }

    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public CartAdditionDTO add(CartAddCmd c) {
        members.requireActive(c.memberId());
        CheckoutRules.id(c.productId());
        String key = CheckoutRules.text(c.requestNo(), 64);
        if (c.quantity() < 1 || c.quantity() > 999)
            throw new IllegalArgumentException("加购数量必须为1至999");
        checkout.lockMember(c.memberId());
        var old = storefront.addition(c.memberId(), key);
        if (old != null) {
            if (!old.productId().equals(c.productId()) || old.quantity() != c.quantity())
                throw new IllegalStateException("请求编号已用于不同的加购内容");
            return new CartAdditionDTO(key, c.productId(), old.quantity(), old.result().quantity());
        }
        var product = products.findOnShelf(c.productId());
        if (product == null
                || product.stock() == null
                || product.price() == null
                || product.price().signum() <= 0) throw new IllegalArgumentException("商品不存在或已下架");
        var line =
                checkout.cart(c.memberId()).stream()
                        .filter(v -> v.productId().equals(c.productId()))
                        .findFirst()
                        .orElse(null);
        int quantity = Math.addExact(line == null ? 0 : line.quantity(), c.quantity());
        if (quantity > 999 || quantity > product.stock())
            throw new IllegalStateException("数量超过当前库存或购物车上限");
        var result = checkout.saveCart(c.memberId(), c.productId(), quantity, true);
        storefront.addition(
                new CartAddition(c.memberId(), key, c.productId(), c.quantity(), result));
        return new CartAdditionDTO(key, c.productId(), c.quantity(), quantity);
    }
}
