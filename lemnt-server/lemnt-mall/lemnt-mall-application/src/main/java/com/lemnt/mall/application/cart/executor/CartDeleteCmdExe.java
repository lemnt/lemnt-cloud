package com.lemnt.mall.application.cart.executor;

import com.lemnt.mall.application.cart.cmd.CartDeleteCmd;
import com.lemnt.mall.application.cart.service.CartService;
import com.lemnt.mall.domain.cart.model.CartRules;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/**
 * 购物车删除命令执行器。
 */
@Component
@RequiredArgsConstructor
public class CartDeleteCmdExe {
    private final CartService cartService;

    /**
     * 执行购物车删除命令。
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean execute(CartDeleteCmd cmd) {
        if (cmd == null) {
            throw new IllegalArgumentException("购物车参数不能为空");
        }
        CartRules.id(cmd.memberId());
        CartRules.id(cmd.productId());
        return cartService.deleteCart(cmd.memberId(), cmd.productId());
    }
}
