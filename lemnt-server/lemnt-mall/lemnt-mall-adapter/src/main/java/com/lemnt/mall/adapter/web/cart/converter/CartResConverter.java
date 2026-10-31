package com.lemnt.mall.adapter.web.cart.converter;

import com.lemnt.mall.adapter.web.cart.res.MallCartListRes;
import com.lemnt.mall.adapter.web.cart.res.MallCartSaveRes;
import com.lemnt.mall.application.cart.dto.CartItemDTO;

/** 按业务域转换接口与应用数据。 */
public final class CartResConverter {
    private CartResConverter() {}

    /**
     * 购物车明细。
     */
    public static MallCartListRes cartItemToMallCartListRes(CartItemDTO value) {
        return value == null
                ? null
                : new MallCartListRes(
                value.id(),
                value.productId(),
                value.quantity(),
                value.selected(),
                value.version(),
                value.name(),
                value.productCode(),
                value.unitPrice(),
                value.availableStock(),
                value.sellable());
    }

    /**
     * 购物车明细。
     */
    public static MallCartSaveRes cartItemToMallCartSaveRes(CartItemDTO value) {
        return value == null
                ? null
                : new MallCartSaveRes(
                value.id(),
                value.productId(),
                value.quantity(),
                value.selected(),
                value.version(),
                value.name(),
                value.productCode(),
                value.unitPrice(),
                value.availableStock(),
                value.sellable());
    }
}
