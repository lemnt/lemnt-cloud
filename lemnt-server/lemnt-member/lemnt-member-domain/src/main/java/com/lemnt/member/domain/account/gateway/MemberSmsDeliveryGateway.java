package com.lemnt.member.domain.account.gateway;

public interface MemberSmsDeliveryGateway {
    boolean configured();

    String hash(String mobile, String scene, String code);

    void send(String mobile, String code);

    int dailyLimit();
}
