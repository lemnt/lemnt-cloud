package com.lemnt.trade.application.checkout.executor;

import com.lemnt.trade.application.checkout.cmd.*;
import com.lemnt.trade.application.checkout.dto.*;
import com.lemnt.trade.application.checkout.converter.CheckoutDTOConverter;
import com.lemnt.trade.domain.checkout.gateway.CheckoutGateway;
import com.lemnt.trade.domain.checkout.model.*;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CouponListQueryExe {
    private final CheckoutGateway checkout;

    public java.util.List<CouponDTO> execute(Long memberId) {
        CheckoutRules.id(memberId);
        return checkout.coupons(memberId).stream().map(CheckoutDTOConverter::coupon).toList();
    }
}
