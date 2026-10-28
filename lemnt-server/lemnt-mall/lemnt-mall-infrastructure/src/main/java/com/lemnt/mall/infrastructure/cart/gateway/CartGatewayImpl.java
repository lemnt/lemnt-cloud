package com.lemnt.mall.infrastructure.cart.gateway;

import com.lemnt.mall.domain.cart.gateway.CartGateway;
import com.lemnt.mall.domain.cart.model.CartLine;
import com.lemnt.mall.domain.cart.model.CartReference;
import com.lemnt.mall.infrastructure.cart.converter.CartConverter;
import com.lemnt.mall.infrastructure.cart.dataobject.CartDO;
import com.lemnt.mall.infrastructure.cart.mapper.CartMapper;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
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
        var values = cartMapper.selectList(Wrappers.<CartDO>lambdaQuery()
                .eq(CartDO::getMemberId, memberId)
                .orderByAsc(CartDO::getId)
                .last("LIMIT 201"));
        if (values.size() > 200) {
            throw new IllegalStateException("购物车条目超过200");
        }
        return values.stream().map(CartConverter::cart).toList();
    }

    /** 保存购物车条目，由应用层先获取会员结算锁。 */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public CartLine saveCart(Long memberId, Long productId, int quantity, boolean selected) {
        var row = cartMapper.selectOne(Wrappers.<CartDO>lambdaQuery()
                .eq(CartDO::getMemberId, memberId)
                .eq(CartDO::getProductId, productId));
        if (row == null) {
            if (cart(memberId).size() >= 200) {
                throw new IllegalArgumentException("购物车最多200项商品");
            }
            row = new CartDO();
            row.setMemberId(memberId);
            row.setProductId(productId);
            row.setQuantity(quantity);
            row.setSelected(selected);
            cartMapper.insert(row);
        } else {
            row.setQuantity(quantity);
            row.setSelected(selected);
            if (cartMapper.update(null, Wrappers.<CartDO>lambdaUpdate()
                .set(CartDO::getQuantity, row.getQuantity()).set(CartDO::getSelected, row.getSelected())
                .setIncrBy(CartDO::getVersion, 1).eq(CartDO::getId, row.getId())
                .eq(CartDO::getMemberId, row.getMemberId())) != 1) {
                throw new IllegalStateException("购物车已变化");
            }
        }
        return CartConverter.cart(cartMapper.selectOne(Wrappers.<CartDO>lambdaQuery()
                .eq(CartDO::getMemberId, memberId)
                .eq(CartDO::getProductId, productId)));
    }

    /** 删除购物车条目，由应用层先获取会员结算锁。 */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteCart(Long memberId, Long productId) {
        return cartMapper.delete(Wrappers.<CartDO>lambdaQuery()
                .eq(CartDO::getMemberId, memberId)
                .eq(CartDO::getProductId, productId)) == 1;
    }

    /** 消费已提交的购物车版本。 */
    @Override
    public void consume(Long memberId, CartReference reference) {
        cartMapper.delete(Wrappers.<CartDO>lambdaQuery()
                .eq(CartDO::getMemberId, memberId)
                .eq(CartDO::getId, reference.cartId())
                .eq(CartDO::getVersion, reference.cartVersion()));
    }
}
