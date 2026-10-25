package com.lemnt.member.infrastructure.account.gateway;

import com.lemnt.member.domain.account.gateway.MemberSmsDeliveryGateway;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsRequest;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

@org.springframework.stereotype.Component
public class AliyunMemberSmsDeliveryGateway implements MemberSmsDeliveryGateway {
    private final String key, secret, sign, template, pepper;
    private final int dailyLimit;

    public AliyunMemberSmsDeliveryGateway(
            @org.springframework.beans.factory.annotation.Value("${member.sms.access-key-id:}")
                    String key,
            @org.springframework.beans.factory.annotation.Value("${member.sms.access-key-secret:}")
                    String secret,
            @org.springframework.beans.factory.annotation.Value("${member.sms.sign-name:}")
                    String sign,
            @org.springframework.beans.factory.annotation.Value("${member.sms.template-code:}")
                    String template,
            @org.springframework.beans.factory.annotation.Value("${member.sms.pepper:}")
                    String pepper,
            @org.springframework.beans.factory.annotation.Value("${member.sms.daily-limit:100}")
                    int dailyLimit) {
        this.key = key;
        this.secret = secret;
        this.sign = sign;
        this.template = template;
        this.pepper = pepper;
        this.dailyLimit = dailyLimit;
    }

    public boolean configured() {
        return !key.isBlank()
                && !secret.isBlank()
                && !sign.isBlank()
                && !template.isBlank()
                && pepper.getBytes(StandardCharsets.UTF_8).length >= 32
                && dailyLimit > 0;
    }

    public int dailyLimit() {
        return dailyLimit;
    }

    public String hash(String mobile, String scene, String code) {
        if (!configured()) throw new UnsupportedOperationException();
        try {
            var mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(pepper.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of()
                    .formatHex(
                            mac.doFinal(
                                    (mobile + ":" + scene + ":" + code)
                                            .getBytes(StandardCharsets.UTF_8)));
        } catch (java.security.GeneralSecurityException ex) {
            throw new IllegalStateException("验证码校验不可用");
        }
    }

    public void send(String mobile, String code) {
        if (!configured()) throw new UnsupportedOperationException();
        if (!mobile.matches("1[3-9][0-9]{9}"))
            throw new IllegalArgumentException("当前短信渠道只支持中国大陆手机号");
        var request = new SendSmsRequest();
        request.setSysProtocol(com.aliyuncs.http.ProtocolType.HTTPS);
        request.setPhoneNumbers(mobile);
        request.setSignName(sign);
        request.setTemplateCode(template);
        request.setTemplateParam("{\"code\":\"" + code + "\"}");
        request.setSysConnectTimeout(3000);
        request.setSysReadTimeout(5000);
        var client = new DefaultAcsClient(DefaultProfile.getProfile("cn-hangzhou", key, secret));
        try {
            var response = client.getAcsResponse(request);
            if (response == null || !"OK".equals(response.getCode()))
                throw new IllegalStateException("短信发送未确认，请稍后重试");
        } catch (com.aliyuncs.exceptions.ClientException ex) {
            throw new IllegalStateException("短信发送未确认，请稍后重试");
        } finally {
            client.shutdown();
        }
    }
}
