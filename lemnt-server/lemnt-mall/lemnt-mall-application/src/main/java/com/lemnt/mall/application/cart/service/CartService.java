package com.lemnt.mall.application.cart.service;

import com.lemnt.mall.domain.cart.gateway.CartGateway;
import com.lemnt.mall.domain.cart.model.CartLine;
import com.lemnt.mall.domain.member.gateway.MemberLockGateway;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/** 购物车写操作编排。 */
@Component
@RequiredArgsConstructor
public class CartService {
    private final MemberLockGateway memberLockGateway;
    private final CartGateway cartGateway;

    /** 在会员操作锁内保存购物车。 */
    @Transactional(rollbackFor = Exception.class)
    public CartLine saveCart(Long memberId, Long productId, int quantity, boolean selected) {
        memberLockGateway.lockMember(memberId);
        return cartGateway.saveCart(memberId, productId, quantity, selected);
    }

    /** 在会员操作锁内删除购物车。 */
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteCart(Long memberId, Long productId) {
        memberLockGateway.lockMember(memberId);
        return cartGateway.deleteCart(memberId, productId);
    }
}
