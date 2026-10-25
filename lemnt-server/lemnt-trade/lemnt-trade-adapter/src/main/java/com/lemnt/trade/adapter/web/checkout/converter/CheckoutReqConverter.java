package com.lemnt.trade.adapter.web.checkout.converter;

import com.lemnt.trade.adapter.web.checkout.req.*;
import com.lemnt.trade.application.checkout.cmd.*;
import com.lemnt.trade.application.checkout.qry.CheckoutQuoteQry;

public final class CheckoutReqConverter {
    private CheckoutReqConverter() {}

    public static CartSaveCmd cart(Long memberId, Long productId, CartSaveReq req) {
        return new CartSaveCmd(memberId, productId, req.quantity(), req.selected());
    }

    public static CouponTemplateCreateCmd template(CouponTemplateCreateReq req) {
        return new CouponTemplateCreateCmd(
                req.code(),
                req.name(),
                req.kind(),
                req.threshold(),
                req.fixedAmount(),
                req.discountPercent(),
                req.discountLimit(),
                req.totalCount(),
                req.validFrom(),
                req.validUntil());
    }

    public static CouponTemplateEnableCmd enable(Long id, CouponTemplateEnableReq req) {
        return new CouponTemplateEnableCmd(id, req.enabled());
    }

    public static CheckoutQuoteQry quote(Long memberId, CheckoutQuoteReq req) {
        return new CheckoutQuoteQry(memberId, req.productIds(), req.couponId());
    }

    public static CheckoutSubmitCmd checkout(Long memberId, CheckoutSubmitReq req) {
        return new CheckoutSubmitCmd(
                memberId, req.requestNo(), req.productIds(), req.couponId(), req.quoteToken());
    }
}
