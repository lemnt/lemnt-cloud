package com.lemnt.member.application.account.service;

import com.lemnt.member.domain.account.model.*;
import com.lemnt.member.domain.account.gateway.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.*;

@Component
@RequiredArgsConstructor
public class MemberAuthService {
    private final MemberAccountGateway accounts;
    private final MemberPasswordGateway passwords;
    private static final SecureRandom RANDOM = new SecureRandom();

    private static String hash(String value) {
        try {
            return HexFormat.of()
                    .formatHex(
                            MessageDigest.getInstance("SHA-256")
                                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private String token(String prefix) {
        byte[] value = new byte[32];
        RANDOM.nextBytes(value);
        return prefix + Base64.getUrlEncoder().withoutPadding().encodeToString(value);
    }

    private String parse(String value, String prefix) {
        if (value == null || !value.matches(prefix + "[A-Za-z0-9_-]{43}"))
            throw new SecurityException("会员会话无效");
        return hash(value);
    }

    private Profile active(Long id) {
        var user = accounts.lock(id);
        if (user == null || user.status() != 1) throw new SecurityException("会员会话无效");
        return user;
    }

    private TokenPair issue(Long memberId) {
        var now = accounts.now();
        String access = token("mbr_a_");
        String refresh = token("mbr_r_");
        accounts.sessionSave(
                new Session(
                        UUID.randomUUID().toString(),
                        memberId,
                        hash(access),
                        hash(refresh),
                        now.plusHours(2),
                        now.plusDays(30)));
        return new TokenPair(memberId, access, refresh, 7200L);
    }

    @Transactional(noRollbackFor = SecurityException.class)
    public TokenPair login(String mobile, String password) {
        String phone = MemberRules.mobile(mobile);
        if (password == null || password.getBytes(StandardCharsets.UTF_8).length > 72)
            throw new SecurityException("手机号或密码错误");
        String loginHash = hash(phone);
        var attempt = accounts.loginAttempt(loginHash);
        var now = accounts.now();
        if (!attempt.windowStart().plusMinutes(15).isAfter(now))
            attempt = new LoginAttempt(loginHash, 0, now);
        if (attempt.failures() >= 5) throw new SecurityException("登录尝试过多，请稍后再试");
        var initial = accounts.credential(phone);
        Profile user = initial == null ? null : accounts.lock(initial.memberId());
        var credential = user == null ? null : accounts.credentialForMember(user.id());
        boolean matched =
                passwords.matches(password, credential == null ? null : credential.passwordHash());
        if (!matched || user == null || user.status() != 1 || !phone.equals(user.mobile())) {
            accounts.loginAttemptUpdate(
                    new LoginAttempt(loginHash, attempt.failures() + 1, attempt.windowStart()));
            throw new SecurityException("手机号或密码错误");
        }
        accounts.loginAttemptUpdate(new LoginAttempt(loginHash, 0, now));
        return issue(user.id());
    }

    public Long authenticate(String accessToken) {
        String key;
        try {
            key = parse(accessToken, "mbr_a_");
        } catch (SecurityException ex) {
            return null;
        }
        var session = accounts.sessionAccess(key);
        if (session == null || !session.expiresAt().isAfter(accounts.now())) return null;
        var user = accounts.profile(session.memberId());
        return user != null && user.status() == 1 ? user.id() : null;
    }

    public Long require(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer "))
            throw new SecurityException("会员会话无效");
        Long id = authenticate(authorization.substring(7));
        if (id == null) throw new SecurityException("会员会话无效");
        return id;
    }

    @Transactional(rollbackFor = Exception.class)
    public TokenPair refresh(String refreshToken) {
        String key = parse(refreshToken, "mbr_r_");
        var initial = accounts.sessionRefresh(key);
        if (initial == null) throw new SecurityException("会员会话无效");
        active(initial.memberId());
        var session = accounts.lockSession(key);
        if (session == null || !session.refreshExpiresAt().isAfter(accounts.now()))
            throw new SecurityException("会员会话无效");
        accounts.sessionDelete(session.id());
        return issue(session.memberId());
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean logout(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer "))
            throw new SecurityException("会员会话无效");
        String key = parse(authorization.substring(7), "mbr_a_");
        var session = accounts.sessionAccess(key);
        if (session == null) return true;
        accounts.lock(session.memberId());
        accounts.sessionDelete(session.id());
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean resetPassword(Long memberId, String password) {
        MemberRules.id(memberId);
        MemberRules.password(password);
        if (accounts.lock(memberId) == null) throw new IllegalArgumentException("会员不存在");
        accounts.password(memberId, passwords.encode(password));
        accounts.sessionsRevoke(memberId);
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean changePassword(Long memberId, String oldPassword, String newPassword) {
        active(memberId);
        MemberRules.password(newPassword);
        var credential = accounts.credentialForMember(memberId);
        if (oldPassword == null
                || !passwords.matches(
                        oldPassword, credential == null ? null : credential.passwordHash()))
            throw new SecurityException("原密码错误");
        accounts.password(memberId, passwords.encode(newPassword));
        accounts.sessionsRevoke(memberId);
        return true;
    }

    /** Internal boundary: callers must complete SMS/provider proof before invoking this. */
    @Transactional(rollbackFor = Exception.class)
    public TokenPair issueAfterVerifiedIdentity(Long memberId) {
        active(memberId);
        return issue(memberId);
    }
}
