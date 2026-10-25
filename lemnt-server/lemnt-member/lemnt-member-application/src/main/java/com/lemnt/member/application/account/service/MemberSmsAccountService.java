package com.lemnt.member.application.account.service;

import com.lemnt.member.domain.account.gateway.MemberAccountGateway;
import com.lemnt.member.domain.account.model.MemberRules;

@org.springframework.stereotype.Component
@lombok.RequiredArgsConstructor
public class MemberSmsAccountService {
    private final MemberAccountGateway accounts;
    private final MemberSmsService delivery;
    private final MemberSmsChallengeService challenge;

    private void authorize(Long memberId, String mobile, String scene) {
        MemberRules.mobile(mobile);
        if ("UPDATE_OLD_MOBILE".equals(scene) || "UPDATE_NEW_MOBILE".equals(scene)) {
            var user = accounts.profile(MemberRules.id(memberId));
            if (user == null || user.status() != 1) throw new SecurityException("会员会话无效");
            if ("UPDATE_OLD_MOBILE".equals(scene) && !mobile.equals(user.mobile()))
                throw new SecurityException("会员手机号不匹配");
        } else if (!java.util.Set.of("LOGIN", "RESET_PASSWORD").contains(scene))
            throw new IllegalArgumentException("短信场景不合法");
    }

    public boolean send(Long memberId, String mobile, String scene) {
        authorize(memberId, mobile, scene);
        return delivery.send(mobile, scene);
    }

    public boolean validate(Long memberId, String mobile, String scene, String code) {
        authorize(memberId, mobile, scene);
        return challenge.verify(mobile, scene, code, false);
    }
}
