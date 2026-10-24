package com.lemnt.gateway.handler;

import com.lemnt.common.core.utils.JsonUtils;
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;

import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;

import static org.assertj.core.api.Assertions.assertThat;

class SentinelFallbackHandlerTest {

    @Test
    void shouldReturnRateLimitCodeWithoutExpiringSession() {
        var exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/system/users/current-info"));

        new SentinelFallbackHandler().handle(exchange, new FlowException("gateway-flow")).block();

        var body = JsonUtils.toJsonObj(exchange.getResponse().getBodyAsString().block());
        assertThat(body.getIntValue("code")).isEqualTo(429);
        assertThat(body.getString("msg")).isEqualTo("请求超过最大数，请稍候再试");
    }
}
