package com.lemnt.gateway.filter;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import com.lemnt.gateway.config.properties.XssProperties;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * XSS 过滤器测试
 *
 * <p>核心断言：过滤器检测到 XSS 内容后必须拒绝请求并抛出异常。</p>
 *
 * @author wangxx
 */
class XssFilterTest {

    /**
     * 含脚本标签的请求体，用于验证清洗确实生效
     */
    private static final String XSS_BODY = "{\"name\":\"<script>alert(1)</script>\"}";

    private final XssFilter filter = new XssFilter();

    private final XssProperties properties = new XssProperties();

    @BeforeEach
    void setUp() {
        properties.setEnabled(true);
        properties.setExcludeUrls(List.of());
        injectProperties();
    }

    @Test
    @DisplayName("JSON 请求体包含 XSS：抛出请求异常")
    void shouldRejectJsonBodyContainingXss() {
        MockServerWebExchange exchange = jsonRequest(HttpMethod.POST, "/api/test");

        assertThatThrownBy(() -> forward(exchange))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.BAD_REQUEST))
                .hasMessageContaining("请求包含不安全内容");
    }

    @Test
    @DisplayName("视频通知的正常属性：原样转发，不误判 controls/contenteditable")
    void shouldKeepNoticeVideoAttributes() {
        String body = """
                {"noticeContent":"<video class=\\"ql-video\\" controls=\\"\\" contenteditable=\\"false\\" src=\\"/files/clip.mp4\\"></video>"}
                """;
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/system/notices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(body));

        assertThat(forward(exchange)).isEqualTo(body);
    }

    @Test
    @DisplayName("嵌入视频通知的固定属性：原样转发")
    void shouldKeepNoticeIframeAttributes() {
        String body = """
                {"noticeContent":"<iframe class='ql-video' title='通知视频' contenteditable='false' src='https://www.youtube.com/embed/M7lc1UVf-VE?autoplay=0' sandbox='allow-scripts allow-same-origin allow-presentation' allowfullscreen></iframe>"}
                """;
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.put("/system/notices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(body));

        assertThat(forward(exchange)).isEqualTo(body);
    }

    @Test
    @DisplayName("真实事件属性和脚本协议：仍拒绝请求")
    void shouldRejectEventAttributesAndScriptUrls() {
        for (String html : List.of(
                "<img src='x' onerror='alert(1)'>",
                "<video controls ONCANPLAY = 'alert(1)'></video>",
                "<img/onload=alert(1)>",
                "<a href='javascript:alert(1)'>链接</a>")) {
            MockServerWebExchange exchange = MockServerWebExchange.from(
                    MockServerHttpRequest.post("/system/notices")
                            .contentType(MediaType.APPLICATION_JSON)
                            .body("{\"noticeContent\":\"" + html + "\"}"));

            assertThatThrownBy(() -> forward(exchange))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("请求包含不安全内容");
        }
    }

    @Test
    @DisplayName("非 JSON 请求体：原样转发")
    void shouldKeepNonJsonBody() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/api/test")
                        .contentType(MediaType.TEXT_PLAIN)
                        .body(XSS_BODY));

        String forwarded = forward(exchange);

        assertThat(forwarded).isEqualTo(XSS_BODY);
    }

    @Test
    @DisplayName("GET 请求：原样转发")
    void shouldKeepGetBody() {
        MockServerWebExchange exchange = jsonRequest(HttpMethod.GET, "/api/test");

        String forwarded = forward(exchange);

        assertThat(forwarded).isEqualTo(XSS_BODY);
    }

    @Test
    @DisplayName("开关关闭：原样转发")
    void shouldKeepBodyWhenDisabled() {
        properties.setEnabled(false);
        MockServerWebExchange exchange = jsonRequest(HttpMethod.POST, "/api/test");

        String forwarded = forward(exchange);

        assertThat(forwarded).isEqualTo(XSS_BODY);
    }

    @Test
    @DisplayName("命中排除路径：原样转发")
    void shouldKeepBodyWhenUrlExcluded() {
        properties.setExcludeUrls(List.of("/api/test"));
        MockServerWebExchange exchange = jsonRequest(HttpMethod.POST, "/api/test");

        String forwarded = forward(exchange);

        assertThat(forwarded).isEqualTo(XSS_BODY);
    }

    /**
     * 执行过滤器并捕获实际转发给下游的请求体
     */
    private String forward(MockServerWebExchange exchange) {
        AtomicReference<String> captured = new AtomicReference<>();
        GatewayFilterChain chain = filtered -> DataBufferUtils.join(filtered.getRequest().getBody())
                .doOnNext(buffer -> {
                    byte[] bytes = new byte[buffer.readableByteCount()];
                    buffer.read(bytes);
                    DataBufferUtils.release(buffer);
                    captured.set(new String(bytes, StandardCharsets.UTF_8));
                })
                .then();

        filter.filter(exchange, chain).block();

        return captured.get();
    }

    private MockServerWebExchange jsonRequest(HttpMethod method, String path) {
        return MockServerWebExchange.from(
                MockServerHttpRequest.method(method, path)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(XSS_BODY));
    }

    /**
     * XssFilter 采用字段注入，测试中直接写入属性对象
     */
    private void injectProperties() {
        ReflectionTestUtils.setField(filter, "xss", properties);
    }
}
