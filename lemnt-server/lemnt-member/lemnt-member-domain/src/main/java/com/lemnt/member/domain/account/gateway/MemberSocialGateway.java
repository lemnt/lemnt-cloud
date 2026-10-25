package com.lemnt.member.domain.account.gateway;

import com.lemnt.member.domain.account.model.SocialProof;
import java.util.List;

public interface MemberSocialGateway {
    String namespace(String provider);

    SocialProof login(String provider, String loginCode, String phoneCode);

    String phone(String code);

    String authorizationUrl(String provider, String state);

    byte[] qrcode(String scene, String page);

    List<com.lemnt.member.domain.account.model.SubscribeTemplate> subscribeTemplates();

    com.lemnt.member.domain.account.model.JsapiConfig jsapiSignature(String url);
}
