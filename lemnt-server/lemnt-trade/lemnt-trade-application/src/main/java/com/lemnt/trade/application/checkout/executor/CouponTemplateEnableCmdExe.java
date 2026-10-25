package com.lemnt.trade.application.checkout.executor;

import com.lemnt.trade.application.checkout.cmd.*;
import com.lemnt.trade.application.checkout.dto.*;
import com.lemnt.trade.domain.checkout.gateway.CheckoutGateway;
import com.lemnt.trade.domain.checkout.model.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CouponTemplateEnableCmdExe {
    private final CheckoutGateway checkout;

    @Transactional(rollbackFor = Exception.class)
    public boolean execute(CouponTemplateEnableCmd cmd) {

        if (cmd == null) throw new IllegalArgumentException("模板参数不能为空");
        CheckoutRules.id(cmd.id());
        return checkout.enableTemplate(cmd.id(), cmd.enabled());
    }
}
