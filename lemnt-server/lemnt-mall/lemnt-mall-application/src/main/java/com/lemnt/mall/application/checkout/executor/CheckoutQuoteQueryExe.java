package com.lemnt.mall.application.checkout.executor;

import com.lemnt.mall.application.checkout.converter.CheckoutDTOConverter;
import com.lemnt.mall.application.checkout.dto.CheckoutQuoteDTO;
import com.lemnt.mall.application.checkout.qry.CheckoutQuoteQry;
import com.lemnt.mall.application.checkout.service.OrderPlacementService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 结算报价查询执行器。
 */
@Component
@RequiredArgsConstructor
public class CheckoutQuoteQueryExe {

    private final OrderPlacementService orderPlacementService;

    /**
     * 执行结算报价查询。
     */
    public CheckoutQuoteDTO execute(
            CheckoutQuoteQry query) {
        return CheckoutDTOConverter.quote(orderPlacementService.quote(query));
    }
}
