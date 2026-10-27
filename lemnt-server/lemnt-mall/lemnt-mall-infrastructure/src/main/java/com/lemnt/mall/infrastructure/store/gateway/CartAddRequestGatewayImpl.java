package com.lemnt.mall.infrastructure.store.gateway;

import com.lemnt.mall.domain.checkout.model.CartLine;
import com.lemnt.mall.domain.store.gateway.CartAddRequestGateway;
import com.lemnt.mall.domain.store.model.CartAddition;
import com.lemnt.mall.infrastructure.store.dataobject.CartAdditionDO;
import com.lemnt.mall.infrastructure.store.mapper.CartAddRequestMapper;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** CartAddRequest 表持久化实现。 */
@Component
@RequiredArgsConstructor
public class CartAddRequestGatewayImpl implements CartAddRequestGateway {
    private final CartAddRequestMapper cartAddRequestMapper;

    @Override
    /**
     * 增量加购。
     */
    public CartAddition addition(Long m, String key) {
        var v = cartAddRequestMapper.addition(m, key);
        return v == null
                ? null
                : new CartAddition(
                v.getMemberId(),
                v.getRequestNo(),
                v.getProductId(),
                v.getQuantity(),
                new CartLine(
                v.getCartId(),
                v.getMemberId(),
                v.getProductId(),
                v.getResultQuantity(),
                v.isSelected(),
                v.getCartVersion()));
    }

    @Override
    /**
     * 增量加购。
     */
    public void addition(CartAddition v) {
        var row = new CartAdditionDO();
        row.setMemberId(v.memberId());
        row.setRequestNo(v.requestNo());
        row.setProductId(v.productId());
        row.setQuantity(v.quantity());
        row.setCartId(v.result().id());
        row.setResultQuantity(v.result().quantity());
        row.setSelected(v.result().selected());
        row.setCartVersion(v.result().version());
        cartAddRequestMapper.additionSave(row);
    }
}
