package com.lemnt.gateway.filter;

import com.lemnt.common.core.utils.StringUtils;
import com.lemnt.common.security.constant.SecurityConstants;

import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Timer;
import org.jspecify.annotations.NonNull;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * 网关访问日志过滤器
 * <p>
 * 记录每次请求的 traceId、method、path、status、duration、clientIp，
 * 并通过 Micrometer 记录 gateway.request.duration 指标
 * </p>
 */
@Slf4j
@Component
public class AccessLogFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, @NonNull GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        long startTime = System.nanoTime();

        String traceId = request.getHeaders().getFirst(SecurityConstants.HEADER_TRACE_ID);
        String method = request.getMethod().name();
        String path = request.getURI().getPath();
        String clientIp = getClientIp(request);

        return chain.filter(exchange)
            .then(
                Mono.fromRunnable(
                    () -> {
                        long durationMs =
                            TimeUnit.NANOSECONDS.toMillis(
                                System.nanoTime() - startTime);
                        ServerHttpResponse response = exchange.getResponse();
                        int statusCode = 0;
                        HttpStatusCode httpStatusCode = response.getStatusCode();
                        if (httpStatusCode != null) {
                            statusCode = httpStatusCode.value();
                        }

                        // 结构化访问日志
                        log.info(
                            "ACCESS: traceId={}, method={}, path={}, status={}, duration={}ms, clientIp={}",
                            traceId,
                            method,
                            path,
                            statusCode,
                            durationMs,
                            clientIp);

                        // 记录到 Micrometer 指标
                        Timer.builder("gateway.request.duration")
                            .tag("method", method)
                            .tag("status", String.valueOf(statusCode))
                            .description("网关请求耗时")
                            .register(Metrics.globalRegistry)
                            .record(Duration.ofMillis(durationMs));
                    }));
    }

    /**
     * 获取客户端真实 IP
     */
    private String getClientIp(ServerHttpRequest request) {
        // 优先从代理头获取
        String xff = request.getHeaders().getFirst("X-Forwarded-For");
        if (StringUtils.isNotBlank(xff)) {
            return xff.split(",")[0].trim();
        }
        String xri = request.getHeaders().getFirst("X-Real-IP");
        if (StringUtils.isNotBlank(xri)) {
            return xri;
        }
        // 兜底：从远程地址获取
        InetSocketAddress remoteAddress = request.getRemoteAddress();
        if (remoteAddress != null && remoteAddress.getAddress() != null) {
            return remoteAddress.getAddress().getHostAddress();
        }
        return "unknown";
    }

    @Override
    public int getOrder() {
        // 在 TraceFilter(-500) 之后，AuthFilter(2) 之前
        return -2;
    }
}
