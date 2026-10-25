package com.lemnt.trade.domain.store.gateway;

import com.lemnt.trade.domain.store.model.CartAddition;
import com.lemnt.trade.domain.checkout.model.*;
import java.util.List;

public interface StorefrontGateway {
    boolean claimable(Long id);

    void claimable(Long id, boolean enabled);

    List<Long> publicTemplateIds();

    List<CouponTemplate> offers(int offset, int limit);

    long offerCount();

    Coupon issued(Long memberId, Long templateId);

    List<Coupon> wallet(Long memberId, int offset, int limit);

    long walletCount(Long memberId);

    CartAddition addition(Long memberId, String requestNo);

    void addition(CartAddition value);
}
