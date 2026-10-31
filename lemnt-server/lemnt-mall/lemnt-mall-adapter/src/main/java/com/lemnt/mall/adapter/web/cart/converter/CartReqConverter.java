package com.lemnt.mall.adapter.web.cart.converter;

import com.lemnt.mall.adapter.web.cart.req.CartSaveReq;
import com.lemnt.mall.application.cart.cmd.CartSaveCmd;

/** 按业务域转换接口与应用数据。 */
public final class CartReqConverter {
    private CartReqConverter() {}

    /**
     * 购物车。
     */
    public static CartSaveCmd cart(Long memberId, Long productId, CartSaveReq req) {
        return new CartSaveCmd(memberId, productId, req.quantity(), req.selected());
    }
}
