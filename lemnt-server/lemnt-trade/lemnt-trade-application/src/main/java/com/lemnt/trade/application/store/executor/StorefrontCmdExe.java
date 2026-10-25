package com.lemnt.trade.application.store.executor;

import com.lemnt.trade.application.store.cmd.*;
import com.lemnt.trade.application.store.dto.CartAdditionDTO;
import com.lemnt.trade.application.checkout.dto.CouponDTO;
import com.lemnt.trade.application.store.service.StorefrontService;

@org.springframework.stereotype.Component
@lombok.RequiredArgsConstructor
public class StorefrontCmdExe {
    private final StorefrontService service;

    public CartAdditionDTO execute(CartAddCmd c) {
        return service.add(c);
    }

    public CouponDTO execute(CouponClaimCmd c) {
        return service.claim(c);
    }

    public boolean execute(CouponPublicClaimCmd c) {
        return service.publicClaim(c);
    }
}
