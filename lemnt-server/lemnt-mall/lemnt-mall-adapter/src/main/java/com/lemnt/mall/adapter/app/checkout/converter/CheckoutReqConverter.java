package com.lemnt.mall.adapter.app.checkout.converter;

import com.lemnt.mall.adapter.app.checkout.req.CartSaveReq;
import com.lemnt.mall.adapter.app.checkout.req.CheckoutQuoteReq;
import com.lemnt.mall.application.checkout.cmd.CartSaveCmd;

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
     * 报价。
     */
    public static com.lemnt.mall.application.checkout.cmd.SettlementCreateCmd quote(Long memberId,
            CheckoutQuoteReq req) {
        return new com.lemnt.mall.application.checkout.cmd.SettlementCreateCmd(memberId,
                req.productIds(),
                req.couponId());
    }
}
