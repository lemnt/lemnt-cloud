package com.lemnt.mall.application.checkout.service;

import com.lemnt.mall.domain.checkout.model.CheckoutRules;
import com.lemnt.mall.domain.member.gateway.MemberLookupGateway;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 结算会员服务。
 */
@Component
@RequiredArgsConstructor
public class CheckoutMemberService {
    private final MemberLookupGateway memberLookupGateway;

    /**
     * 校验有效。
     */
    public void requireActive(Long memberId) {
        CheckoutRules.id(memberId);
        var member = memberLookupGateway.findById(memberId);
        if (member == null || !Integer.valueOf(1).equals(member.status())) {
            throw new IllegalArgumentException("会员不存在或已禁用");
        }
    }
}
