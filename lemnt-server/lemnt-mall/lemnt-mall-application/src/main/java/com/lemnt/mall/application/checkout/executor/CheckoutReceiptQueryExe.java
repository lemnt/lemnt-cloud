package com.lemnt.mall.application.checkout.executor;

import com.lemnt.mall.application.checkout.converter.CheckoutDTOConverter;
import com.lemnt.mall.application.checkout.dto.CheckoutReceiptDTO;
import com.lemnt.mall.application.checkout.service.CheckoutService;
import com.lemnt.mall.domain.checkout.model.CheckoutRules;
import com.lemnt.mall.domain.order.gateway.OrderGateway;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 结算回执查询执行器。
 */
@Component
@RequiredArgsConstructor
public class CheckoutReceiptQueryExe {
    private final CheckoutService checkoutService;
    private final OrderGateway orderPersistenceService;

    /**
     * 执行结算回执查询。
     */
    public CheckoutReceiptDTO execute(String orderNo) {
        String key = CheckoutRules.text(orderNo, 40);
        return CheckoutDTOConverter.receipt(checkoutService.receipt(key), orderPersistenceService.findByOrderNo(key));
    }

    /**
     * 执行结算回执查询。
     */
    public CheckoutReceiptDTO execute(Long memberId, String orderNo) {
        CheckoutRules.id(memberId);
        String key = CheckoutRules.text(orderNo, 40);
        var order = orderPersistenceService.findByOrderNo(key);
        if (order == null || !memberId.equals(order.memberId())) {
            return null;
        }
        var record = checkoutService.receipt(
                key
        );
        if (record == null || !memberId.equals(record.memberId())) {
            return null;
        }
        return CheckoutDTOConverter.receipt(record, order);
    }
}
