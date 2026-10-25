package com.lemnt.member.application.account.service;

import com.lemnt.member.domain.account.gateway.MemberSocialGateway;
import com.lemnt.member.domain.account.model.*;
import com.lemnt.member.application.account.dto.SocialStateDTO;

@org.springframework.stereotype.Component
@lombok.RequiredArgsConstructor
public class MemberSocialService {
    private final MemberIdentityService identities;
    private final MemberSocialGateway provider;

    public SocialStateDTO state(String type, String purpose, Long memberId) {
        String namespace = provider.namespace(type);
        String state = identities.state(namespace, purpose, memberId);
        return new SocialStateDTO(
                state, "WECHAT_MP".equals(type) ? provider.authorizationUrl(type, state) : null);
    }

    public TokenPair login(String type, String loginCode, String phoneCode, String state) {
        String namespace = provider.namespace(type);
        var proof = provider.login(type, MemberRules.text(loginCode, 256), phoneCode);
        return identities.socialLogin(namespace, proof, state);
    }

    public boolean bind(Long memberId, String type, String code, String state) {
        String namespace = provider.namespace(type);
        var proof = provider.login(type, MemberRules.text(code, 256), null);
        return identities.bind(memberId, namespace, proof, state);
    }

    public boolean unbind(Long memberId, String type) {
        return identities.unbind(memberId, provider.namespace(type));
    }

    public boolean mobile(Long memberId, String code) {
        return identities.wechatMobile(memberId, provider.phone(MemberRules.text(code, 256)));
    }

    public byte[] qrcode(String scene, String page) {
        return provider.qrcode(MemberRules.text(scene, 32), MemberRules.text(page, 128));
    }

    public java.util.List<com.lemnt.member.domain.account.model.SubscribeTemplate> templates() {
        return provider.subscribeTemplates();
    }

    public com.lemnt.member.domain.account.model.JsapiConfig signature(String url) {
        return provider.jsapiSignature(url);
    }
}
