package com.lemnt.member.infrastructure.user.gateway;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.common.core.utils.JsonUtils;
import com.lemnt.member.domain.user.gateway.WechatLoginGateway;
import com.lemnt.member.infrastructure.user.config.MemberWechatProperties;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

/**
 * 调用微信小程序登录接口用一次性凭证换取 openid；凭证与会话密钥均不在本服务保存。
 */
@Component
@RequiredArgsConstructor
public class WechatLoginGatewayImpl implements WechatLoginGateway {

    private static final String JSCODE_SESSION_URL = "https://api.weixin.qq.com/sns/jscode2session";
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(5);
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(10);

    private final MemberWechatProperties properties;
    private final HttpClient httpClient =
        HttpClient.newBuilder().connectTimeout(CONNECT_TIMEOUT).build();

    @Override
    public String exchangeOpenId(String code) {
        if (code == null || code.isBlank()) {
            throw new ServiceException("微信登录凭证不能为空", 400);
        }
        String appId = properties.getAppId();
        String secret = properties.getSecret();
        if (appId == null || appId.isBlank() || secret == null || secret.isBlank()) {
            throw new ServiceException("微信登录未配置小程序凭证", 500);
        }
        String url =
            JSCODE_SESSION_URL
                + "?appid="
                + encode(appId)
                + "&secret="
                + encode(secret)
                + "&js_code="
                + encode(code)
                + "&grant_type=authorization_code";
        HttpResponse<String> response;
        try {
            response =
                httpClient.send(
                    HttpRequest.newBuilder(URI.create(url)).timeout(REQUEST_TIMEOUT).GET().build(),
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (IOException exception) {
            throw new ServiceException("微信登录请求失败", 500);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new ServiceException("微信登录请求被中断", 500);
        }
        if (response.statusCode() != 200) {
            throw new ServiceException("微信登录响应异常", 500);
        }
        Map<String, Object> body = JsonUtils.toMap(response.body());
        if (body == null) {
            throw new ServiceException("微信登录响应不是合法JSON", 500);
        }
        Object errorCode = body.get("errcode");
        if (errorCode != null && !"0".equals(String.valueOf(errorCode))) {
            throw new ServiceException("微信登录失败：" + body.get("errmsg"), 400);
        }
        Object openId = body.get("openid");
        if (openId == null || String.valueOf(openId).isBlank()) {
            throw new ServiceException("微信登录未返回openid", 500);
        }
        return String.valueOf(openId);
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
