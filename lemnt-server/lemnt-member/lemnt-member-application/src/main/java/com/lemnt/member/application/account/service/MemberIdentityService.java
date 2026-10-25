package com.lemnt.member.application.account.service;

import com.lemnt.member.domain.account.model.*;
import com.lemnt.member.domain.account.gateway.*;
import com.lemnt.member.domain.gateway.MemberGateway;
import com.lemnt.member.domain.model.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.security.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Component
@RequiredArgsConstructor
public class MemberIdentityService {
    private final MemberVerificationGateway verification;
    private final MemberAccountGateway accounts;
    private final MemberGateway members;
    private final MemberAuthService authentication;
    private final MemberSmsChallengeService sms;

    private String hash(String text) {
        try {
            return HexFormat.of()
                    .formatHex(
                            MessageDigest.getInstance("SHA-256")
                                    .digest(text.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private Profile active(Long id) {
        var user = accounts.lock(MemberRules.id(id));
        if (user == null || user.status() != 1) throw new SecurityException("会员会话无效");
        return user;
    }

    @Transactional(rollbackFor = Exception.class)
    public String state(String namespace, String purpose, Long memberId) {
        MemberRules.text(namespace, 100);
        if (purpose == null || !Set.of("LOGIN", "BIND").contains(purpose))
            throw new IllegalArgumentException("授权目的不合法");
        if ("BIND".equals(purpose)) active(memberId);
        else if (memberId != null) throw new IllegalArgumentException("登录state不能指定会员");
        byte[] random = new byte[24];
        new SecureRandom().nextBytes(random);
        String state = Base64.getUrlEncoder().withoutPadding().encodeToString(random);
        verification.stateSave(
                new SocialState(
                        hash(state), namespace, purpose, memberId, accounts.now().plusMinutes(5)));
        return state;
    }

    private void consume(String state, String namespace, String purpose, Long memberId) {
        if (state == null || !state.matches("[A-Za-z0-9_-]{32}"))
            throw new SecurityException("授权state无效");
        var value = verification.lockState(hash(state));
        if (value == null
                || !value.expiresAt().isAfter(accounts.now())
                || !namespace.equals(value.provider())
                || !purpose.equals(value.purpose())
                || !Objects.equals(memberId, value.memberId()))
            throw new SecurityException("授权state无效");
        verification.stateDelete(value.stateHash());
    }

    private Long register(String mobile) {
        var existing = mobile == null ? null : members.findByMobile(mobile);
        if (existing != null) return existing.id();
        String no =
                "M"
                        + UUID.randomUUID()
                                .toString()
                                .replace("-", "")
                                .substring(0, 24)
                                .toUpperCase(Locale.ROOT);
        var value =
                mobile == null
                        ? new Member(null, no, "微信会员", null, null, null, 1, 0L, null)
                        : Member.register(
                                no,
                                "会员" + mobile.substring(Math.max(0, mobile.length() - 4)),
                                mobile,
                                null,
                                null);
        return members.save(value).id();
    }

    private SocialProof proof(SocialProof v, String namespace) {
        if (v == null || !namespace.equals(v.provider())) throw new SecurityException("第三方身份校验失败");
        MemberRules.text(v.subject(), 200);
        if (v.mobile() != null) MemberRules.mobile(v.mobile());
        return v;
    }

    @Transactional(rollbackFor = Exception.class)
    public TokenPair socialLogin(String namespace, SocialProof input, String state) {
        var p = proof(input, namespace);
        accounts.lockCatalog();
        consume(state, namespace, "LOGIN", null);
        var binding = verification.binding(namespace, p.subject());
        Long memberId;
        if (binding != null) {
            memberId = binding.memberId();
            var member = active(memberId);
            if (p.mobile() != null && !p.mobile().equals(member.mobile()))
                throw new SecurityException("手机号与既有绑定不一致，请先核验账户");
        } else {
            memberId = register(p.mobile());
            active(memberId);
            verification.bind(new SocialBinding(namespace, p.subject(), memberId));
        }
        return authentication.issueAfterVerifiedIdentity(memberId);
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean bind(Long memberId, String namespace, SocialProof input, String state) {
        var p = proof(input, namespace);
        accounts.lockCatalog();
        active(memberId);
        consume(state, namespace, "BIND", memberId);
        var current = verification.binding(namespace, p.subject());
        if (current != null) {
            if (!memberId.equals(current.memberId()))
                throw new IllegalStateException("第三方身份已绑定其他会员");
            return true;
        }
        verification.bind(new SocialBinding(namespace, p.subject(), memberId));
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean unbind(Long memberId, String namespace) {
        accounts.lockCatalog();
        var user = active(memberId);
        var all = verification.bindings(memberId);
        if (all.stream().noneMatch(v -> namespace.equals(v.provider()))) return false;
        if (all.size() == 1
                && (user.mobile() == null || accounts.credentialForMember(memberId) == null))
            throw new IllegalStateException("解绑前请先设置可用手机号及密码，避免账户无法登录");
        return verification.unbind(memberId, namespace);
    }

    public List<String> bindings(Long memberId) {
        MemberRules.id(memberId);
        return verification.bindings(memberId).stream()
                .map(v -> v.provider().split(":", 2)[0])
                .toList();
    }

    @Transactional(noRollbackFor = SecurityException.class)
    public TokenPair smsLogin(String mobile, String code) {
        accounts.lockCatalog();
        sms.verify(mobile, "LOGIN", code, true);
        Long id = register(MemberRules.mobile(mobile));
        active(id);
        return authentication.issueAfterVerifiedIdentity(id);
    }

    @Transactional(noRollbackFor = SecurityException.class)
    public boolean resetPassword(String mobile, String code, String password) {
        MemberRules.password(password);
        accounts.lockCatalog();
        sms.verify(mobile, "RESET_PASSWORD", code, true);
        var user = members.findByMobile(MemberRules.mobile(mobile));
        if (user == null) throw new SecurityException("会员校验失败");
        active(user.id());
        return authentication.resetPassword(user.id(), password);
    }

    private void replaceMobile(Long memberId, String mobile) {
        var other = members.findByMobile(mobile);
        if (other != null && !memberId.equals(other.id()))
            throw new IllegalStateException("手机号已被其他会员使用");
        verification.updateMobile(memberId, mobile);
        accounts.sessionsRevoke(memberId);
    }

    @Transactional(noRollbackFor = SecurityException.class)
    public boolean mobile(Long memberId, String mobile, String code, String oldCode) {
        accounts.lockCatalog();
        var user = active(memberId);
        mobile = MemberRules.mobile(mobile);
        if (user.mobile() != null) sms.verify(user.mobile(), "UPDATE_OLD_MOBILE", oldCode, false);
        sms.verify(mobile, "UPDATE_NEW_MOBILE", code, false);
        if (user.mobile() != null) sms.verify(user.mobile(), "UPDATE_OLD_MOBILE", oldCode, true);
        sms.verify(mobile, "UPDATE_NEW_MOBILE", code, true);
        replaceMobile(memberId, mobile);
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean wechatMobile(Long memberId, String mobile) {
        accounts.lockCatalog();
        active(memberId);
        replaceMobile(memberId, MemberRules.mobile(mobile));
        return true;
    }
}
