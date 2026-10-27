package com.lemnt.mall.application.checkout.executor;

import com.lemnt.mall.application.checkout.cmd.CartSaveCmd;
import com.lemnt.mall.application.checkout.converter.CheckoutDTOConverter;
import com.lemnt.mall.application.checkout.dto.CartItemDTO;
import com.lemnt.mall.application.checkout.service.CheckoutMemberService;
import com.lemnt.mall.application.checkout.service.CheckoutService;
import com.lemnt.mall.domain.catalog.gateway.ProductCatalogGateway;
import com.lemnt.mall.domain.checkout.gateway.CartGateway;
import com.lemnt.mall.domain.checkout.gateway.CheckoutAccountGateway;
import com.lemnt.mall.domain.checkout.model.CheckoutRules;

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
    private final CheckoutAccountGateway checkoutAccountGateway;
    private final CheckoutService checkoutService;
    private final CheckoutMemberService checkoutMemberService;
    private final ProductCatalogGateway productCatalogGateway;

    /**
     * 执行购物车保存命令。
     */
    @Transactional(rollbackFor = Exception.class)
    public CartItemDTO execute(CartSaveCmd cmd) {
        if (cmd == null || cmd.quantity() < 1 || cmd.quantity() > 999) {
            throw new IllegalArgumentException("购物车数量必须为1至999");
        }
        CheckoutRules.id(cmd.productId());
        checkoutMemberService.requireActive(cmd.memberId());
        checkoutAccountGateway.lockMember(cmd.memberId());
        var snapshot = productCatalogGateway.findOnShelf(cmd.productId());
        if (snapshot == null
                &&(cmd.selected()
                || cartGateway.cart(cmd.memberId()).stream()
                .noneMatch(row -> row.productId().equals(cmd.productId())))) {
            throw new IllegalArgumentException("商品不存在或已下架，只能取消已有条目的选中状态");
        }
        return CheckoutDTOConverter.cart(
                checkoutService.saveCart(cmd.memberId(), cmd.productId(), cmd.quantity(), cmd.selected()),
                snapshot);
    }
}
