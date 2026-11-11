package com.lemnt.promotion.infrastructure.cart.converter;

import com.lemnt.promotion.domain.cart.model.CartLine;
import com.lemnt.promotion.infrastructure.cart.dataobject.CartDO;

/**
 * 按业务域转换接口与应用数据。
 */
public final class CartConverter {
    private CartConverter() {
    }

    /**
     * 购物车。
     */
    public static CartLine cart(CartDO row) {
        return row == null
            ? null
            : new CartLine(
            row.getId(),
            row.getMemberId(),
            row.getProductId(),
            row.getQuantity(),
            Boolean.TRUE.equals(row.getSelected()),
            row.getVersion());
    }
}
