package com.lemnt.member.application.account.service;

import com.lemnt.member.domain.account.gateway.MemberSmsDeliveryGateway;

@org.springframework.stereotype.Component
@lombok.RequiredArgsConstructor
public class MemberSmsService {
    private final MemberSmsChallengeService challenge;
    private final MemberSmsDeliveryGateway delivery;

    public boolean send(String mobile, String scene) {
        String code = challenge.prepare(mobile, scene);
        delivery.send(mobile, code);
        return true;
    }
}
