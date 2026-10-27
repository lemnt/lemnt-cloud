package com.lemnt.mall.application.checkout.executor;

import com.lemnt.mall.application.checkout.cmd.CheckoutDirectCmd;
import com.lemnt.mall.application.checkout.service.OrderPlacementService;
import com.lemnt.mall.domain.order.model.OrderSnapshot;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 交易订单创建命令执行器。
 */
@Component
@RequiredArgsConstructor
public class CheckoutDirectCmdExe {
    private final OrderPlacementService orderPlacementService;

    /**
     * 执行交易订单创建命令。
     */
    public OrderSnapshot execute(CheckoutDirectCmd cmd) {
        return orderPlacementService.createDirect(cmd);
    }
}
