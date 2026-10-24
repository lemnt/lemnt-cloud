package com.lemnt.gateway.handler;

import com.lemnt.common.core.utils.JsonUtils;

import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ResponseStatusException;
import reactor.netty.http.client.PrematureCloseException;

import java.net.SocketException;

import static org.assertj.core.api.Assertions.assertThat;

class GatewayExceptionHandlerTest {

    private final GatewayExceptionHandler handler = new GatewayExceptionHandler();

    @Test
    void shouldNotExpireSessionWhenUpstreamConnectionFails() {
        assertResponse(new SocketException("Connection reset"), 502, "后端服务连接中断，请重试");
        assertResponse(PrematureCloseException.TEST_EXCEPTION,
                502, "后端服务连接中断，请重试");
    }

    @Test
    void shouldReturnServiceUnavailableWhenNoInstanceIsRegistered() {
        assertResponse(new NotFoundException("Unable to find instance for lemnt-system"),
                503, "服务暂不可用，请稍后重试");
    }

    @Test
    void shouldKeepRequestErrorAndAuthenticationStatusSeparate() {
        assertResponse(new ResponseStatusException(HttpStatus.BAD_REQUEST, "请求包含不安全内容"),
                400, "请求包含不安全内容");
        assertResponse(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "登录失效"),
                401, "登录失效");
        assertResponse(new IllegalStateException("Unexpected failure"), 500, "内部服务器错误");
    }

    private void assertResponse(Throwable error, int expectedCode, String expectedMessage) {
        var exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/system/users/current-info"));

        handler.handle(exchange, error).block();

        var body = JsonUtils.toJsonObj(exchange.getResponse().getBodyAsString().block());
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(body.getIntValue("code")).isEqualTo(expectedCode);
        assertThat(body.getString("msg")).isEqualTo(expectedMessage);
    }
}
