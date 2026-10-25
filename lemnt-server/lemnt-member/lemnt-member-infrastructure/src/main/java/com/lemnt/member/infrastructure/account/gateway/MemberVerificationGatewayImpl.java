package com.lemnt.member.infrastructure.account.gateway;

import com.lemnt.member.domain.account.gateway.MemberVerificationGateway;
import com.lemnt.member.domain.account.model.*;
import com.lemnt.member.infrastructure.account.mapper.MemberVerificationMapper;
import com.lemnt.member.infrastructure.account.converter.*;
import java.util.List;

@org.springframework.stereotype.Component
@lombok.RequiredArgsConstructor
public class MemberVerificationGatewayImpl implements MemberVerificationGateway {
    private final MemberVerificationMapper mapper;

    public SmsChallenge lockSms(String mobile, String scene) {
        mapper.ensureSms(mobile, scene);
        return SmsChallengeConverter.domain(mapper.lockSms(mobile, scene));
    }

    public void smsSave(SmsChallenge value) {
        mapper.smsSave(SmsChallengeConverter.row(value));
    }

    public void smsBudget(java.time.LocalDate day, int limit) {
        mapper.ensureBudget(day);
        if (mapper.lockBudget(day) >= limit) throw new IllegalStateException("短信日额度已用尽");
        mapper.addBudget(day);
    }

    public void stateSave(SocialState value) {
        mapper.stateSave(SocialStateConverter.row(value));
    }

    public SocialState lockState(String hash) {
        return SocialStateConverter.domain(mapper.lockState(hash));
    }

    public void stateDelete(String hash) {
        mapper.stateDelete(hash);
    }

    public SocialBinding binding(String provider, String subject) {
        return SocialBindingConverter.domain(mapper.binding(provider, subject));
    }

    public List<SocialBinding> bindings(Long memberId) {
        return mapper.bindings(memberId).stream().map(SocialBindingConverter::domain).toList();
    }

    public void bind(SocialBinding value) {
        mapper.bind(SocialBindingConverter.row(value));
    }

    public boolean unbind(Long memberId, String provider) {
        return mapper.unbind(memberId, provider) == 1;
    }

    public boolean updateMobile(Long memberId, String mobile) {
        return mapper.updateMobile(memberId, mobile) == 1;
    }
}
