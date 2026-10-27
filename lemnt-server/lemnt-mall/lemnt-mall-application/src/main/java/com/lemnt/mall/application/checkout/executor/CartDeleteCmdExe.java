package com.lemnt.mall.application.checkout.executor;

import com.lemnt.mall.application.checkout.cmd.CartDeleteCmd;
import com.lemnt.mall.application.checkout.service.CheckoutService;
import com.lemnt.mall.domain.checkout.model.CheckoutRules;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/**
 * 购物车删除命令执行器。
 */
@Component
@RequiredArgsConstructor
public class CartDeleteCmdExe {
    private final CheckoutService checkoutService;

    /**
     * 执行购物车删除命令。
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean execute(CartDeleteCmd cmd) {
        if (cmd == null) {
            throw new IllegalArgumentException("购物车参数不能为空");
        }
        CheckoutRules.id(cmd.memberId());
        CheckoutRules.id(cmd.productId());
        return checkoutService.deleteCart(cmd.memberId(), cmd.productId());
    }
}
