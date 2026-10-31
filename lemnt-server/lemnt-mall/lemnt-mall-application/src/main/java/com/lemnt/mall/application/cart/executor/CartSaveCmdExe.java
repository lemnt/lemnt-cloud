package com.lemnt.mall.application.cart.executor;

import com.lemnt.mall.application.cart.cmd.CartSaveCmd;
import com.lemnt.mall.application.cart.converter.CartDTOConverter;
import com.lemnt.mall.application.cart.dto.CartItemDTO;
import com.lemnt.mall.application.cart.service.CartService;
import com.lemnt.mall.application.member.service.MemberValidationService;
import com.lemnt.mall.domain.cart.gateway.CartGateway;
import com.lemnt.mall.domain.cart.model.CartRules;
import com.lemnt.mall.domain.member.gateway.MemberLockGateway;
import com.lemnt.mall.domain.product.gateway.ProductCatalogGateway;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/**
 * 购物车保存命令执行器。
 */
@Component
@RequiredArgsConstructor
public class CartSaveCmdExe {
    private final CartGateway cartGateway;
    private final MemberLockGateway memberLockGateway;
    private final CartService cartService;
    private final MemberValidationService memberValidationService;
    private final ProductCatalogGateway productCatalogGateway;

    /**
     * 执行购物车保存命令。
     */
    @Transactional(rollbackFor = Exception.class)
    public CartItemDTO execute(CartSaveCmd cmd) {
        if (cmd == null || cmd.quantity() < 1 || cmd.quantity() > 999) {
            throw new IllegalArgumentException("购物车数量必须为1至999");
        }
        CartRules.id(cmd.productId());
        memberValidationService.requireActive(cmd.memberId());
        memberLockGateway.lockMember(cmd.memberId());
        var snapshot = productCatalogGateway.findOnShelf(cmd.productId());
        if (snapshot == null
                &&(cmd.selected()
                || cartGateway.cart(cmd.memberId()).stream()
                .noneMatch(row -> row.productId().equals(cmd.productId())))) {
            throw new IllegalArgumentException("商品不存在或已下架，只能取消已有条目的选中状态");
        }
        if (snapshot != null && (snapshot.stock() == null || cmd.quantity() > snapshot.stock())) {
            throw new IllegalStateException("数量超过当前库存");
        }
        return CartDTOConverter.cart(
                cartService.saveCart(cmd.memberId(), cmd.productId(), cmd.quantity(), cmd.selected()),
                snapshot);
    }
}
