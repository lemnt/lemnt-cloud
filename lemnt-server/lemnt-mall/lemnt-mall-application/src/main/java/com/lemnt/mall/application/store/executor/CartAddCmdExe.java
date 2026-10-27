package com.lemnt.mall.application.store.executor;

import com.lemnt.mall.application.store.cmd.CartAddCmd;
import com.lemnt.mall.application.store.dto.CartAdditionDTO;
import com.lemnt.mall.application.store.service.StorefrontService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 商城命令执行器。
 */
@Component
@RequiredArgsConstructor
public class CartAddCmdExe {
    private final StorefrontService storefrontService;

    /**
     * 执行购物车新增命令。
     */
    public CartAdditionDTO execute(CartAddCmd c) {
        return storefrontService.add(c);
    }
}
