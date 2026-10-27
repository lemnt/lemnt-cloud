package com.lemnt.mall.adapter.web.checkout.converter;

import com.lemnt.mall.adapter.web.checkout.req.CartSaveReq;
import com.lemnt.mall.adapter.web.checkout.req.CheckoutQuoteReq;
import com.lemnt.mall.adapter.web.checkout.req.CheckoutSubmitReq;
import com.lemnt.mall.adapter.web.checkout.req.CouponTemplateCreateReq;
import com.lemnt.mall.adapter.web.checkout.req.CouponTemplateEnableReq;
import com.lemnt.mall.application.checkout.cmd.CartSaveCmd;
import com.lemnt.mall.application.checkout.cmd.CheckoutSubmitCmd;
import com.lemnt.mall.application.checkout.cmd.CouponTemplateCreateCmd;
import com.lemnt.mall.application.checkout.cmd.CouponTemplateEnableCmd;
import com.lemnt.mall.application.checkout.qry.CheckoutQuoteQry;

/**
 * 结算请求转换器。
 */
public final class CheckoutReqConverter {
    private CheckoutReqConverter() {}

    /**
     * 购物车。
     */
    public static CartSaveCmd cart(Long memberId, Long productId, CartSaveReq req) {
        return new CartSaveCmd(memberId, productId, req.quantity(), req.selected());
    }

    /**
     * 模板。
     */
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

    /**
     * 启用。
     */
    public static CouponTemplateEnableCmd enable(Long id, CouponTemplateEnableReq req) {
        return new CouponTemplateEnableCmd(id, req.enabled());
    }

    /**
     * 报价。
     */
    public static CheckoutQuoteQry quote(Long memberId, CheckoutQuoteReq req) {
        return new CheckoutQuoteQry(memberId, req.productIds(), req.couponId());
    }

    /**
     * 结算。
     */
    public static CheckoutSubmitCmd checkout(Long memberId, CheckoutSubmitReq req) {
        return new CheckoutSubmitCmd(
                memberId, req.requestNo(), req.productIds(), req.couponId(), req.quoteToken());
    }
}
