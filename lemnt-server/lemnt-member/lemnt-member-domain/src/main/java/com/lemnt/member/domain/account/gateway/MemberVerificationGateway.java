package com.lemnt.member.domain.account.gateway;

import com.lemnt.member.domain.account.model.*;
import java.util.List;

public interface MemberVerificationGateway {
    SmsChallenge lockSms(String mobile, String scene);

    void smsSave(SmsChallenge value);

    void smsBudget(java.time.LocalDate day, int limit);

    void stateSave(SocialState value);

    SocialState lockState(String hash);

    void stateDelete(String hash);

    SocialBinding binding(String provider, String subject);

    List<SocialBinding> bindings(Long memberId);

    void bind(SocialBinding value);

    boolean unbind(Long memberId, String provider);

    boolean updateMobile(Long memberId, String mobile);
}
