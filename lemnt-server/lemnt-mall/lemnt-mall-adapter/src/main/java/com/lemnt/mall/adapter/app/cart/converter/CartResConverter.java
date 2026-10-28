package com.lemnt.mall.adapter.app.cart.converter;

import com.lemnt.mall.adapter.app.cart.res.MallAppCartCartRes;
import com.lemnt.mall.adapter.app.cart.res.MallAppCartSaveRes;
import com.lemnt.mall.application.cart.dto.CartItemDTO;

/** 按业务域转换接口与应用数据。 */
public final class CartResConverter {
    private CartResConverter() {}

    /**
     * 购物车明细。
     */
    public static MallAppCartCartRes cartItemToMallAppCartCartRes(CartItemDTO value) {
        return value == null
                ? null
                : new MallAppCartCartRes(
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
    public static MallAppCartSaveRes cartItemToMallAppCartSaveRes(CartItemDTO value) {
        return value == null
                ? null
                : new MallAppCartSaveRes(
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
