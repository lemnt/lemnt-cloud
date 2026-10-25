package com.lemnt.trade.infrastructure.store.gateway;

import com.lemnt.trade.domain.store.gateway.StorefrontGateway;
import com.lemnt.trade.domain.store.model.CartAddition;
import com.lemnt.trade.domain.checkout.model.*;
import com.lemnt.trade.infrastructure.store.mapper.StorefrontMapper;
import com.lemnt.trade.infrastructure.store.dataobject.CartAdditionDO;
import com.lemnt.trade.infrastructure.checkout.converter.CheckoutConverter;
import java.util.List;

@org.springframework.stereotype.Component
@lombok.RequiredArgsConstructor
public class StorefrontGatewayImpl implements StorefrontGateway {
    private final StorefrontMapper mapper;

    public boolean claimable(Long id) {
        return Boolean.TRUE.equals(mapper.claimable(id));
    }

    public void claimable(Long id, boolean enabled) {
        mapper.setClaimable(id, enabled);
    }

    public List<Long> publicTemplateIds() {
        var rows = mapper.publicTemplateIds();
        if (rows.size() > 1000) throw new IllegalStateException("公开模板超过1000，请接入分页");
        return rows;
    }

    public List<CouponTemplate> offers(int offset, int limit) {
        return mapper.offers(offset, limit).stream().map(CheckoutConverter::template).toList();
    }

    public long offerCount() {
        return mapper.offerCount();
    }

    public Coupon issued(Long m, Long t) {
        return CheckoutConverter.coupon(mapper.issued(m, t));
    }

    public List<Coupon> wallet(Long m, int o, int l) {
        return mapper.wallet(m, o, l).stream().map(CheckoutConverter::coupon).toList();
    }

    public long walletCount(Long m) {
        return mapper.walletCount(m);
    }

    public CartAddition addition(Long m, String key) {
        var v = mapper.addition(m, key);
        return v == null
                ? null
                : new CartAddition(
                        v.getMemberId(),
                        v.getRequestNo(),
                        v.getProductId(),
                        v.getQuantity(),
                        new CartLine(
                                v.getCartId(),
                                v.getMemberId(),
                                v.getProductId(),
                                v.getResultQuantity(),
                                v.isSelected(),
                                v.getCartVersion()));
    }

    public void addition(CartAddition v) {
        var row = new CartAdditionDO();
        row.setMemberId(v.memberId());
        row.setRequestNo(v.requestNo());
        row.setProductId(v.productId());
        row.setQuantity(v.quantity());
        row.setCartId(v.result().id());
        row.setResultQuantity(v.result().quantity());
        row.setSelected(v.result().selected());
        row.setCartVersion(v.result().version());
        mapper.additionSave(row);
    }
}
