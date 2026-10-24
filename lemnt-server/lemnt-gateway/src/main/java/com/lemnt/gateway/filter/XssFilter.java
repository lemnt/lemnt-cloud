package com.lemnt.gateway.filter;

import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

import jakarta.annotation.Resource;
import org.apache.commons.lang3.Strings;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import com.lemnt.common.core.utils.StringUtils;
import com.lemnt.gateway.config.properties.XssProperties;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 跨站脚本过滤器
 *
 * @author wangxx
 */
@Component
@ConditionalOnProperty(value = "security.xss.enabled", havingValue = "true")
public class XssFilter implements GlobalFilter, Ordered {

    private static final Pattern XSS_PATTERN = Pattern.compile(
            // 事件属性从单词边界开始，避免将 controls/contenteditable 误判为 on* 属性。
            "<script[^>]*>.*?</script>|javascript:|\\bon\\w+\\s*=",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    @Resource
    private XssProperties xss;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        // xss开关未开启 或 通过nacos关闭，不过滤
        if (!xss.getEnabled()) {
            return chain.filter(exchange);
        }
        // GET DELETE 不过滤
        HttpMethod method = request.getMethod();
        if (HttpMethod.GET.equals(method) || HttpMethod.DELETE.equals(method)) {
            return chain.filter(exchange);
        }
        // 非json类型，不过滤
        if (!isJsonRequest(exchange)) {
            return chain.filter(exchange);
        }
        // excludeUrls 不过滤
        String url = request.getURI().getPath();
        if (StringUtils.matches(url, xss.getExcludeUrls())) {
            return chain.filter(exchange);
        }
        return DataBufferUtils.join(request.getBody())
                .flatMap(buffer -> {
                    byte[] bytes = new byte[buffer.readableByteCount()];
                    buffer.read(bytes);
                    DataBufferUtils.release(buffer);
                    String body = new String(bytes, StandardCharsets.UTF_8);
                    if (XSS_PATTERN.matcher(body).find()) {
                        return Mono.error(new ResponseStatusException(
                                HttpStatus.BAD_REQUEST, "请求包含不安全内容"));
                    }
                    ServerHttpRequestDecorator decorator = new ServerHttpRequestDecorator(request) {
                        @Override
                        public Flux<DataBuffer> getBody() {
                            return Flux.defer(() -> Flux.just(
                                    exchange.getResponse().bufferFactory().wrap(bytes)));
                        }

                        @Override
                        public HttpHeaders getHeaders() {
                            HttpHeaders headers = new HttpHeaders();
                            headers.putAll(super.getHeaders());
                            headers.remove(HttpHeaders.CONTENT_LENGTH);
                            return headers;
                        }
                    };

                    return chain.filter(exchange.mutate().request(decorator).build());
                });
    }

    /**
     * 是否是Json请求
     *
     * @param exchange HTTP请求
     */
    public boolean isJsonRequest(ServerWebExchange exchange) {
        String header = exchange.getRequest().getHeaders().getFirst(HttpHeaders.CONTENT_TYPE);
        return Strings.CI.startsWith(header, MediaType.APPLICATION_JSON_VALUE);
    }

    @Override
    public int getOrder() {
        return -300;
    }
}
