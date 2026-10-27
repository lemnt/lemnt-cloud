package com.lemnt.mall.infrastructure.checkout.gateway;

import com.lemnt.mall.domain.checkout.gateway.CartGateway;
import com.lemnt.mall.domain.checkout.model.CartLine;
import com.lemnt.mall.domain.checkout.model.CartReference;
import com.lemnt.mall.infrastructure.checkout.converter.CheckoutConverter;
import com.lemnt.mall.infrastructure.checkout.dataobject.CartDO;
import com.lemnt.mall.infrastructure.checkout.mapper.CartMapper;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import java.util.List;

/** Cart 表持久化实现。 */
@Component
@RequiredArgsConstructor
public class CartGatewayImpl implements CartGateway {
    private final CartMapper cartMapper;

    @Override
    public List<CartLine> cart(Long memberId) {
        var values = cartMapper.cart(memberId);
        if (values.size() > 200) {
            throw new IllegalStateException("购物车条目超过200");
        }
        return values.stream().map(CheckoutConverter::cart).toList();
    }

    /** 保存购物车条目，由应用层先获取会员结算锁。 */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public CartLine saveCart(Long memberId, Long productId, int quantity, boolean selected) {
        var row = cartMapper.cartItem(memberId, productId);
        if (row == null) {
            if (cart(memberId).size() >= 200) {
                throw new IllegalArgumentException("购物车最多200项商品");
            }
            row = new CartDO();
            row.setMemberId(memberId);
            row.setProductId(productId);
            row.setQuantity(quantity);
            row.setSelected(selected);
            cartMapper.insertCart(row);
        } else {
            row.setQuantity(quantity);
            row.setSelected(selected);
            if (cartMapper.updateCart(row) != 1) {
                throw new IllegalStateException("购物车已变化");
            }
        }
        return CheckoutConverter.cart(cartMapper.cartItem(memberId, productId));
    }

    /** 删除购物车条目，由应用层先获取会员结算锁。 */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteCart(Long memberId, Long productId) {
        return cartMapper.deleteCart(memberId, productId) == 1;
    }

    /** 消费已提交的购物车版本。 */
    @Override
    public void consume(Long memberId, CartReference reference) {
        cartMapper.consumeCart(memberId, reference.cartId(), reference.cartVersion());
    }
}
