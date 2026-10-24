package com.lemnt.gateway.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.WebFilter;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** 跨域白名单及预检请求回归测试。 */
class CorsConfigTest {

    private static final String FRONTEND = "http://localhost:5173";

    @Test
    void preflightShouldAllowAuthorizationAndCredentials() {
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest
                .options("http://localhost:8080/system/user")
                .header(HttpHeaders.ORIGIN, FRONTEND)
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "authorization,content-type"));

        assertThat(filter(FRONTEND, exchange)).isFalse();
        HttpHeaders headers = exchange.getResponse().getHeaders();
        // 标准过滤器沿用服务器默认的 200，模拟响应中未显式设置状态。
        assertThat(exchange.getResponse().getStatusCode()).isNull();
        assertThat(headers.getAccessControlAllowOrigin()).isEqualTo(FRONTEND);
        assertThat(headers.getAccessControlAllowCredentials()).isTrue();
        assertThat(headers.getAccessControlAllowHeaders()).contains("authorization", "content-type");
        assertThat(headers.getAccessControlMaxAge()).isEqualTo(18000);
    }

    @Test
    void actualRequestShouldRetainAuthorization() {
        MockServerWebExchange exchange = request(FRONTEND);

        assertThat(filter(FRONTEND, exchange)).isTrue();
        assertThat(exchange.getResponse().getHeaders().getAccessControlAllowOrigin()).isEqualTo(FRONTEND);
        assertThat(exchange.getResponse().getHeaders().getAccessControlAllowCredentials()).isTrue();
        assertThat(exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION)).isEqualTo("Bearer test");
    }

    @Test
    void unlistedOriginShouldBeRejected() {
        MockServerWebExchange exchange = request("https://untrusted.example");

        assertThat(filter(FRONTEND, exchange)).isFalse();
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(exchange.getResponse().getHeaders().getAccessControlAllowOrigin()).isNull();
    }

    @Test
    void unlistedPreflightShouldBeRejected() {
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest
                .options("http://localhost:8080/system/user")
                .header(HttpHeaders.ORIGIN, "https://untrusted.example")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"));

        assertThat(filter(FRONTEND, exchange)).isFalse();
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void unlistedHeaderShouldBeRejected() {
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest
                .options("http://localhost:8080/system/user")
                .header(HttpHeaders.ORIGIN, FRONTEND)
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "X-Untrusted"));

        assertThat(filter(FRONTEND, exchange)).isFalse();
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void emptyWhitelistShouldRejectCrossOriginRequests() {
        MockServerWebExchange exchange = request(FRONTEND);

        assertThat(filter("", exchange)).isFalse();
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void commaSeparatedWhitelistShouldAllowSecondOrigin() {
        MockServerWebExchange exchange = request("http://127.0.0.1:5173");

        assertThat(filter(FRONTEND + ",http://127.0.0.1:5173", exchange)).isTrue();
        assertThat(exchange.getResponse().getHeaders().getAccessControlAllowOrigin())
                .isEqualTo("http://127.0.0.1:5173");
    }

    @Test
    void unlistedMethodShouldBeRejected() {
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest
                .options("http://localhost:8080/system/user")
                .header(HttpHeaders.ORIGIN, FRONTEND)
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "TRACE"));

        assertThat(filter(FRONTEND, exchange)).isFalse();
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void sameOriginShouldPassWithoutCorsHeaders() {
        MockServerWebExchange exchange = request("http://localhost:8080");

        assertThat(filter("", exchange)).isTrue();
        assertThat(exchange.getResponse().getHeaders().getAccessControlAllowOrigin()).isNull();
    }

    @Test
    void wildcardOriginWithCredentialsShouldFailAtStartup() {
        assertThatThrownBy(() -> filter("*", request(FRONTEND)))
                .hasStackTraceContaining("allowCredentials");
    }

    private MockServerWebExchange request(String origin) {
        return MockServerWebExchange.from(MockServerHttpRequest.get("http://localhost:8080/system/user")
                .header(HttpHeaders.ORIGIN, origin)
                .header(HttpHeaders.AUTHORIZATION, "Bearer test"));
    }

    /** 使用实际配置 Bean 执行过滤链，记录请求是否进入后续处理。 */
    private boolean filter(String origins, MockServerWebExchange exchange) {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            TestPropertyValues.of("gateway.cors.allowed-origins=" + origins).applyTo(context);
            context.register(CorsConfig.class);
            context.refresh();
            AtomicBoolean forwarded = new AtomicBoolean();
            context.getBean("corsFilter", WebFilter.class).filter(exchange, request -> {
                forwarded.set(true);
                return Mono.empty();
            }).block();
            return forwarded.get();
        }
    }
}
