package com.lemnt.pay.infrastructure.payment.gateway;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.core.utils.JsonUtils;
import com.lemnt.pay.domain.payment.gateway.PayAppNotifyGateway;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/** 只向部署时授权的接收端发送支付结果。 */
@Component
public class PayAppNotifyGatewayImpl implements PayAppNotifyGateway {
    private static final int TIMEOUT_MILLIS = 5000;
    private static final int MAX_RESPONSE_BYTES = 4096;
    private final Set<String> allowedOrigins;

    public PayAppNotifyGatewayImpl(
            @Value("${payment.app-notify.allowed-origins:}") String configuredOrigins) {
        allowedOrigins =
                Arrays.stream(configuredOrigins.split(","))
                        .map(String::trim)
                        .filter(value -> !value.isEmpty())
                        .map(URI::create)
                        .map(PayAppNotifyGatewayImpl::origin)
                        .collect(Collectors.toUnmodifiableSet());
    }

    /** 拒绝跨地址重定向，任务日志仅记录状态，避免保存回调地址或响应正文。 */
    @Override
    public void post(String url, Map<String, String> payload) {
        URI target;
        try {
            target = URI.create(url);
            if (!allowedOrigins.contains(origin(target))) {
                throw new IllegalArgumentException();
            }
        } catch (IllegalArgumentException | NullPointerException error) {
            throw new IllegalStateException("应用通知地址未获部署授权");
        }
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) target.toURL().openConnection();
            connection.setConnectTimeout(TIMEOUT_MILLIS);
            connection.setReadTimeout(TIMEOUT_MILLIS);
            connection.setInstanceFollowRedirects(false);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);
            byte[] body = JsonUtils.toJsonStr(payload).getBytes(StandardCharsets.UTF_8);
            connection.setFixedLengthStreamingMode(body.length);
            try (var output = connection.getOutputStream()) {
                output.write(body);
            }
            int status = connection.getResponseCode();
            if (status < 200 || status >= 300) {
                throw new IllegalStateException("应用通知收到 HTTP " + status);
            }
            try (var input = connection.getInputStream()) {
                byte[] response = input.readNBytes(MAX_RESPONSE_BYTES + 1);
                verifyAck(response);
            }
        } catch (IOException error) {
            throw new IllegalStateException("应用通知连接或读取失败");
        } finally {
            if (connection != null) connection.disconnect();
        }
    }

    /** 只接受本系统的业务成功码，空响应和异常正文均进入任务重试。 */
    private static void verifyAck(byte[] response) {
        Map<String, Object> ack = null;
        try {
            if (response.length <= MAX_RESPONSE_BYTES) {
                ack = JsonUtils.toMap(new String(response, StandardCharsets.UTF_8));
            }
        } catch (RuntimeException ignored) {
            // 响应内容不进入日志。
        }
        if (ack == null || !Integer.valueOf(R.SUCCESS).equals(ack.get("code"))) {
            throw new IllegalStateException("接收端未确认应用通知");
        }
    }

    private static String origin(URI uri) {
        String scheme = uri.getScheme();
        if (scheme == null
                || uri.getHost() == null
                || uri.getRawUserInfo() != null
                || uri.getRawFragment() != null
                || uri.getPort() == 0
                || uri.getPort() > 65535) {
            throw new IllegalArgumentException("应用通知须使用有效的 HTTP(S) 地址");
        }
        scheme = scheme.toLowerCase(Locale.ROOT);
        if (!"http".equals(scheme) && !"https".equals(scheme)) {
            throw new IllegalArgumentException("应用通知须使用 HTTP(S) 地址");
        }
        int port = uri.getPort() < 0 ? ("https".equals(scheme) ? 443 : 80) : uri.getPort();
        return scheme + "://" + uri.getHost().toLowerCase(Locale.ROOT) + ":" + port;
    }
}
