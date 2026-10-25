package com.lemnt.member.application.account.executor;

import com.lemnt.member.application.account.cmd.*;
import com.lemnt.member.application.account.dto.*;
import com.lemnt.member.application.account.service.*;
import com.lemnt.member.application.account.converter.MemberDTOConverter;

@org.springframework.stereotype.Component
@lombok.RequiredArgsConstructor
public class MemberExternalCmdExe {
    private final MemberSmsAccountService sms;
    private final MemberIdentityService identities;
    private final MemberSocialService social;

    public Boolean execute(SmsSendCmd c) {
        return sms.send(c.memberId(), c.mobile(), c.scene());
    }

    public Boolean execute(SmsValidateCmd c) {
        return sms.validate(c.memberId(), c.mobile(), c.scene(), c.code());
    }

    public TokenPairDTO execute(SmsLoginCmd c) {
        return MemberDTOConverter.tokenPair(identities.smsLogin(c.mobile(), c.code()));
    }

    public Boolean execute(SmsPasswordResetCmd c) {
        return identities.resetPassword(c.mobile(), c.code(), c.password());
    }

    public Boolean execute(MobileUpdateCmd c) {
        return identities.mobile(c.memberId(), c.mobile(), c.code(), c.oldCode());
    }

    public Boolean execute(WechatMobileUpdateCmd c) {
        return social.mobile(c.memberId(), c.code());
    }

    public SocialStateDTO execute(SocialStateCmd c) {
        return social.state(c.provider(), c.purpose(), c.memberId());
    }

    public TokenPairDTO execute(SocialLoginCmd c) {
        return MemberDTOConverter.tokenPair(
                social.login(c.provider(), c.loginCode(), c.phoneCode(), c.state()));
    }

    public Boolean execute(SocialBindCmd c) {
        return social.bind(c.memberId(), c.provider(), c.code(), c.state());
    }

    public Boolean execute(SocialUnbindCmd c) {
        return social.unbind(c.memberId(), c.provider());
    }

    public QrcodeDTO execute(QrcodeCmd c) {
        return new QrcodeDTO(
                java.util.Base64.getEncoder().encodeToString(social.qrcode(c.scene(), c.page())),
                "image/png");
    }

    public JsapiConfigDTO execute(JsapiCmd c) {
        var v = social.signature(c.url());
        return new JsapiConfigDTO(v.appId(), v.nonceStr(), v.timestamp(), v.signature());
    }
}
