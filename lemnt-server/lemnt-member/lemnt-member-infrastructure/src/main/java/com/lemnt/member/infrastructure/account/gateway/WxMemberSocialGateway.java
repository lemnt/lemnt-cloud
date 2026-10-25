package com.lemnt.member.infrastructure.account.gateway;

import com.lemnt.member.domain.account.gateway.MemberSocialGateway;
import com.lemnt.member.domain.account.model.*;
import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.api.impl.WxMaServiceImpl;
import cn.binarywang.wx.miniapp.config.impl.WxMaRedisBetterConfigImpl;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.api.impl.WxMpServiceImpl;
import me.chanjar.weixin.mp.config.impl.WxMpRedisConfigImpl;
import me.chanjar.weixin.common.util.http.apache.DefaultApacheHttpClientBuilder;
import me.chanjar.weixin.common.error.WxErrorException;
import org.springframework.data.redis.core.StringRedisTemplate;
import java.util.*;

@org.springframework.stereotype.Component
public class WxMemberSocialGateway implements MemberSocialGateway {
    private final WxMaService mini;
    private final WxMpService mp;
    private final String miniId, mpId, redirect;
    private final Set<String> jsapiHosts;

    public WxMemberSocialGateway(
            StringRedisTemplate redis,
            @org.springframework.beans.factory.annotation.Value("${member.wechat.mini.app-id:}")
                    String miniId,
            @org.springframework.beans.factory.annotation.Value("${member.wechat.mini.secret:}")
                    String miniSecret,
            @org.springframework.beans.factory.annotation.Value("${member.wechat.mp.app-id:}")
                    String mpId,
            @org.springframework.beans.factory.annotation.Value("${member.wechat.mp.secret:}")
                    String mpSecret,
            @org.springframework.beans.factory.annotation.Value("${member.wechat.mp.redirect-uri:}")
                    String redirect,
            @org.springframework.beans.factory.annotation.Value("${member.wechat.mp.jsapi-hosts:}")
                    String jsapiHosts) {
        this.miniId = miniId;
        this.mpId = mpId;
        this.redirect = redirect;
        this.jsapiHosts =
                Arrays.stream(jsapiHosts.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .collect(java.util.stream.Collectors.toUnmodifiableSet());
        var http = DefaultApacheHttpClientBuilder.get();
        http.setConnectionTimeout(3000);
        http.setConnectionRequestTimeout(3000);
        http.setSoTimeout(5000);
        http.supportedProtocols(new String[] {"TLSv1.2", "TLSv1.3"});
        if (!miniId.isBlank() && !miniSecret.isBlank()) {
            var config =
                    new WxMaRedisBetterConfigImpl(
                            new BoundedWxRedisOps(redis), "member:wechat:mini:" + miniId + ":");
            config.setAppid(miniId);
            config.setSecret(miniSecret);
            config.useStableAccessToken(true);
            config.setApacheHttpClientBuilder(http);
            mini = new WxMaServiceImpl();
            mini.setMaxRetryTimes(0);
            mini.setWxMaConfig(config);
        } else mini = null;
        if (!mpId.isBlank() && !mpSecret.isBlank()) {
            var config =
                    new WxMpRedisConfigImpl(
                            new BoundedWxRedisOps(redis), "member:wechat:mp:" + mpId + ":");
            config.setAppId(mpId);
            config.setSecret(mpSecret);
            config.useStableAccessToken(true);
            var mpHttp = DefaultApacheHttpClientBuilder.get();
            mpHttp.setConnectionTimeout(3000);
            mpHttp.setConnectionRequestTimeout(3000);
            mpHttp.setSoTimeout(5000);
            mpHttp.supportedProtocols(new String[] {"TLSv1.2", "TLSv1.3"});
            config.setApacheHttpClientBuilder(mpHttp);
            mp = new WxMpServiceImpl();
            mp.setMaxRetryTimes(0);
            mp.setWxMpConfigStorage(config);
        } else mp = null;
    }

    public String namespace(String type) {
        if ("WECHAT_MINI".equals(type) && mini != null) return type + ":" + miniId;
        if ("WECHAT_MP".equals(type) && mp != null) return type + ":" + mpId;
        throw new UnsupportedOperationException();
    }

    private RuntimeException error(WxErrorException ex) {
        int code = ex.getError() == null ? -1 : ex.getError().getErrorCode();
        return code == 40029 || code == 40163
                ? new SecurityException("微信凭证无效，请重新获取")
                : new MemberProviderException();
    }

    public SocialProof login(String type, String loginCode, String phoneCode) {
        String namespace = namespace(type);
        try {
            if ("WECHAT_MINI".equals(type)) {
                var info = mini.getUserService().getSessionInfo(loginCode);
                String subject = MemberRules.text(info.getOpenid(), 200);
                return new SocialProof(
                        namespace, subject, phoneCode == null ? null : phone(phoneCode));
            }
            var token = mp.getOAuth2Service().getAccessToken(loginCode);
            return new SocialProof(namespace, MemberRules.text(token.getOpenId(), 200), null);
        } catch (WxErrorException ex) {
            throw error(ex);
        }
    }

    public String phone(String code) {
        namespace("WECHAT_MINI");
        try {
            return MemberRules.mobile(
                    mini.getUserService().getPhoneNoInfo(code).getPurePhoneNumber());
        } catch (WxErrorException ex) {
            throw error(ex);
        }
    }

    public String authorizationUrl(String type, String state) {
        namespace(type);
        if (!"WECHAT_MP".equals(type)) throw new IllegalArgumentException("授权类型不支持跳转");
        var uri = java.net.URI.create(redirect);
        if (!"https".equalsIgnoreCase(uri.getScheme())
                || uri.getHost() == null
                || uri.getFragment() != null) throw new UnsupportedOperationException();
        return mp.getOAuth2Service().buildAuthorizationUrl(redirect, "snsapi_base", state);
    }

    public byte[] qrcode(String scene, String page) {
        namespace("WECHAT_MINI");
        if (!scene.matches("[A-Za-z0-9_.=-]{1,32}") || !page.matches("pages/[A-Za-z0-9/_-]{1,120}"))
            throw new IllegalArgumentException("小程序码参数不合法");
        try {
            byte[] result =
                    mini.getQrcodeService()
                            .createWxaCodeUnlimitBytes(
                                    scene, page, true, "release", 430, true, null, false);
            if (result == null || result.length > 512000) throw new MemberProviderException();
            return result;
        } catch (WxErrorException ex) {
            throw error(ex);
        }
    }

    public List<SubscribeTemplate> subscribeTemplates() {
        namespace("WECHAT_MINI");
        try {
            return mini.getSubscribeService().getTemplateList().stream()
                    .map(
                            v ->
                                    new SubscribeTemplate(
                                            v.getPriTmplId(),
                                            v.getTitle(),
                                            v.getContent(),
                                            v.getType()))
                    .toList();
        } catch (WxErrorException ex) {
            throw error(ex);
        }
    }

    public JsapiConfig jsapiSignature(String url) {
        namespace("WECHAT_MP");
        var uri = java.net.URI.create(MemberRules.text(url, 2048));
        if (!"https".equalsIgnoreCase(uri.getScheme())
                || uri.getHost() == null
                || !jsapiHosts.contains(uri.getHost())
                || uri.getFragment() != null
                || uri.getUserInfo() != null) throw new IllegalArgumentException("JSAPI域名不在允许范围");
        try {
            var v = mp.createJsapiSignature(url);
            return new JsapiConfig(
                    v.getAppId(), v.getNonceStr(), v.getTimestamp(), v.getSignature());
        } catch (WxErrorException ex) {
            throw error(ex);
        }
    }
}
