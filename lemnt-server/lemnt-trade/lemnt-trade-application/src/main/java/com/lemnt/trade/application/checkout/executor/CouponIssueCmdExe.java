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
public class CouponIssueCmdExe {
    private final CheckoutGateway checkout;
    private final com.lemnt.trade.application.checkout.service.CheckoutMemberService members;

    @Transactional(rollbackFor = Exception.class)
    public CouponDTO execute(CouponIssueCmd cmd) {

        if (cmd == null) throw new IllegalArgumentException("领券参数不能为空");
        CheckoutRules.id(cmd.templateId());
        members.requireActive(cmd.memberId());
        return CheckoutDTOConverter.coupon(checkout.issue(cmd.memberId(), cmd.templateId()));
    }
}
