package com.lemnt.member.application.account.service;

import com.lemnt.member.domain.account.gateway.*;
import com.lemnt.member.domain.account.model.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class MemberSmsChallengeService {
    private final MemberVerificationGateway verification;
    private final MemberAccountGateway accounts;
    private final MemberSmsDeliveryGateway delivery;

    private String scene(String value) {
        if (value == null
                || !Set.of("LOGIN", "RESET_PASSWORD", "UPDATE_OLD_MOBILE", "UPDATE_NEW_MOBILE")
                        .contains(value)) throw new IllegalArgumentException("短信场景不合法");
        return value;
    }

    @Transactional(rollbackFor = Exception.class)
    public String prepare(String phone, String input) {
        String mobile = MemberRules.mobile(phone);
        String scene = scene(input);
        if (!delivery.configured()) throw new UnsupportedOperationException();
        var old = verification.lockSms(mobile, scene);
        var now = accounts.now();
        if (old.lastSentAt() != null && old.lastSentAt().plusSeconds(60).isAfter(now))
            throw new IllegalStateException("请稍后再发送");
        int count = now.toLocalDate().equals(old.sentDate()) ? old.dailyCount() : 0;
        if (count >= 20) throw new IllegalStateException("当前手机号今日发送次数已满");
        verification.smsBudget(now.toLocalDate(), delivery.dailyLimit());
        String code =
                String.format(
                        java.util.Locale.ROOT,
                        "%06d",
                        new java.security.SecureRandom().nextInt(1000000));
        verification.smsSave(
                new SmsChallenge(
                        mobile,
                        scene,
                        delivery.hash(mobile, scene, code),
                        now.plusMinutes(5),
                        0,
                        now,
                        now.toLocalDate(),
                        count + 1));
        return code;
    }

    @Transactional(noRollbackFor = SecurityException.class)
    public boolean verify(String phone, String input, String code, boolean consume) {
        String mobile = MemberRules.mobile(phone);
        String scene = scene(input);
        if (!delivery.configured()) throw new UnsupportedOperationException();
        var value = verification.lockSms(mobile, scene);
        var now = accounts.now();
        if (value.codeHash() == null || value.attempts() >= 5 || !value.expiresAt().isAfter(now))
            throw new SecurityException("验证码无效或已过期");
        boolean valid =
                code != null
                        && code.matches("[0-9]{6}")
                        && java.security.MessageDigest.isEqual(
                                value.codeHash().getBytes(java.nio.charset.StandardCharsets.UTF_8),
                                delivery.hash(mobile, scene, code)
                                        .getBytes(java.nio.charset.StandardCharsets.UTF_8));
        verification.smsSave(
                new SmsChallenge(
                        mobile,
                        scene,
                        valid && consume ? null : value.codeHash(),
                        value.expiresAt(),
                        valid ? value.attempts() : value.attempts() + 1,
                        value.lastSentAt(),
                        value.sentDate(),
                        value.dailyCount()));
        if (!valid) throw new SecurityException("验证码无效或已过期");
        return true;
    }
}
