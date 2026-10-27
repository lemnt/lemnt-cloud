package com.lemnt.mall.application.checkout.executor;

import com.lemnt.mall.application.checkout.cmd.CheckoutSubmitCmd;
import com.lemnt.mall.application.checkout.converter.CheckoutDTOConverter;
import com.lemnt.mall.application.checkout.dto.CheckoutReceiptDTO;
import com.lemnt.mall.application.checkout.service.CheckoutService;
import com.lemnt.mall.application.checkout.service.OrderPlacementService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 结算提交命令执行器。
 */
@Component
@RequiredArgsConstructor
public class CheckoutSubmitCmdExe {
    private final CheckoutService checkoutService;
    private final OrderPlacementService orderPlacementService;

    /**
     * 执行结算提交命令。
     */
    public CheckoutReceiptDTO execute(CheckoutSubmitCmd cmd) {
        var order = orderPlacementService.checkoutCart(cmd);
        return CheckoutDTOConverter.receipt(checkoutService.receipt(order.orderNo()), order);
    }
}
