package com.lemnt.mall.application.member.service;

import com.lemnt.mall.domain.member.gateway.MemberLookupGateway;
import com.lemnt.mall.domain.member.model.MemberSnapshot;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 结算会员服务。
 */
@Component
@RequiredArgsConstructor
public class MemberValidationService {
    private final MemberLookupGateway memberLookupGateway;

    /**
     * 校验有效。
     */
    public MemberSnapshot requireActive(Long memberId) {
        if (memberId == null || memberId <= 0) {
            throw new IllegalArgumentException("编号不合法");
        }
        var member = memberLookupGateway.findById(memberId);
        if (member == null || !Integer.valueOf(1).equals(member.status())) {
            throw new IllegalArgumentException("会员不存在或已禁用");
        }
        return member;
    }
}
