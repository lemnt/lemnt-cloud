package com.lemnt.trade.application.checkout.executor;

import com.lemnt.trade.application.checkout.cmd.*;
import com.lemnt.trade.application.checkout.dto.*;
import com.lemnt.trade.application.checkout.converter.CheckoutDTOConverter;
import com.lemnt.trade.domain.checkout.gateway.CheckoutGateway;
import com.lemnt.trade.domain.checkout.model.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CouponTemplateCreateCmdExe {
    private final CheckoutGateway checkout;

    @Transactional(rollbackFor = Exception.class)
    public CouponTemplateDTO execute(CouponTemplateCreateCmd cmd) {

        if (cmd == null) throw new IllegalArgumentException("优惠券参数不能为空");
        var value =
                CheckoutRules.template(
                        new CouponTemplate(
                                null,
                                cmd.code(),
                                cmd.name(),
                                cmd.kind(),
                                cmd.threshold(),
                                cmd.fixedAmount(),
                                cmd.discountPercent(),
                                cmd.discountLimit(),
                                cmd.totalCount(),
                                0,
                                cmd.validFrom(),
                                cmd.validUntil(),
                                true));
        return CheckoutDTOConverter.template(checkout.createTemplate(value));
    }
}
