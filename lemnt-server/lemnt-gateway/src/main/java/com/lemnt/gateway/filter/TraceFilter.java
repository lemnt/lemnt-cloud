package com.lemnt.gateway.filter;

import com.lemnt.common.core.utils.StringUtils;
import com.lemnt.common.security.constant.SecurityConstants;
import com.lemnt.common.snowflake.utils.SnowflakeUtils;
import org.jspecify.annotations.NonNull;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class TraceFilter implements GlobalFilter, Ordered {

    @Autowired
    private SnowflakeUtils snowflakeUtils;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, @NonNull GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        // 获取 TraceId
        String traceId = request.getHeaders().getFirst(SecurityConstants.HEADER_TRACE_ID);
        // 如果没有则生成
        if (StringUtils.isBlank(traceId)) {
            traceId = snowflakeUtils.generateWithPrefix("T");
        }
        // 放入 MDC（当前线程）
        MDC.put(SecurityConstants.TRACE_ID, traceId);
        // 构造新的 Request，透传 TraceId（如果存在）
        ServerHttpRequest newRequest = addTraceId(request, traceId);
        ServerWebExchange newExchange = exchange.mutate()
                .request(newRequest)
                .build();
        return chain.filter(newExchange).doFinally(signal -> MDC.remove(SecurityConstants.TRACE_ID));
    }

    private ServerHttpRequest addTraceId(ServerHttpRequest request, String traceId) {
        return request.mutate().header(SecurityConstants.HEADER_TRACE_ID, traceId)
                .build();
    }

    @Override
    public int getOrder() {
        return -500;
    }
}
