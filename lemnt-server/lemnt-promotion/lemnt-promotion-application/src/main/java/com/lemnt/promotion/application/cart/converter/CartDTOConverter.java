package com.lemnt.promotion.application.cart.converter;

import com.lemnt.promotion.application.cart.dto.CartItemDTO;
import com.lemnt.promotion.domain.cart.model.CartLine;
import com.lemnt.promotion.domain.product.model.ProductSnapshot;

/** 按业务域转换接口与应用数据。 */
public final class CartDTOConverter {
    private CartDTOConverter() {}

    /**
     * 购物车。
     */
    public static CartItemDTO cart(CartLine value, ProductSnapshot product) {
        return new CartItemDTO(
                value.id(),
                value.productId(),
                value.quantity(),
                value.selected(),
                value.version(),
                product == null ? null : product.name(),
                product == null ? null : product.productCode(),
                product == null ? null : product.price(),
                product == null ? null : product.stock(),
                product != null);
    }
}
