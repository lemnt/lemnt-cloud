package com.lemnt.trade.application.checkout.service;

import com.lemnt.trade.domain.gateway.MemberLookupGateway;
import com.lemnt.trade.domain.checkout.model.CheckoutRules;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CheckoutMemberService {
    private final MemberLookupGateway members;

    public void requireActive(Long memberId) {
        CheckoutRules.id(memberId);
        var member = members.findById(memberId);
        if (member == null || !Integer.valueOf(1).equals(member.status()))
            throw new IllegalArgumentException("会员不存在或已禁用");
    }
}
