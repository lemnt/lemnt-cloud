package com.lemnt.gateway.filter;

import com.lemnt.common.core.utils.JsonUtils;
import com.lemnt.common.security.constant.SecurityConstants;
import com.lemnt.gateway.model.OpenApiClient;
import com.lemnt.gateway.service.OpenApiService;
import com.lemnt.gateway.utils.ServletUtils;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.BiFunction;

/**
 * 开放平台授权拦截
 *
 * @author wangxx
 * Since: 2025/10
 */
@Component
public class OpenApiAuthFilter implements GlobalFilter, Ordered {

    @Autowired
    private OpenApiService openApiService;

    private static final String OPEN_API_PREFIX = "/open";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, @NonNull GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        // 只处理开放平台路径
        if (!path.startsWith(OPEN_API_PREFIX)) {
            return chain.filter(exchange);
        }
        HttpHeaders headers = request.getHeaders();
        // 提取加密参数
        String clientId = headers.getFirst(SecurityConstants.CLIENT_ID_HEADER);
        String timestamp = headers.getFirst(SecurityConstants.TIMESTAMP_HEADER);
        String sign = headers.getFirst(SecurityConstants.SIGNATURE_HEADER);
        String nonce = headers.getFirst(SecurityConstants.NONCE_HEADER);
        // 基本参数检查
        if (StringUtils.isAnyBlank(clientId, timestamp, nonce, sign)) {
            return ServletUtils.unauthorized(exchange.getResponse(), "缺少签名参数");
        }
        OpenApiClient client;
        try {
            client = openApiService.getByClientId(clientId);
            if (Objects.isNull(client)) {
                return ServletUtils.unauthorized(exchange.getResponse(), "无效应用ID");
            }
            // 时间戳校验
            openApiService.checkTimestamp(timestamp);
            // 防重放
            openApiService.checkNonce(clientId, nonce);
        } catch (Exception e) {
            return ServletUtils.unauthorized(exchange.getResponse(), e.getMessage());
        }
        // 签名校验
        if (Objects.equals(HttpMethod.GET, request.getMethod())) {
            try {
                Map<String, Object> params = readQuery(request);
                openApiService.verifySign(clientId, timestamp, nonce, client.getAppSecret(), sign, params);
            } catch (Exception e) {
                return ServletUtils.unauthorized(exchange.getResponse(), e.getMessage());
            }
            return chain.filter(addClientHeader(exchange, client));
        } else {
            return readBody(exchange, (body, newExchange) -> {
                try {
                    Map<String, Object> params = JsonUtils.toMap(body);
                    openApiService.verifySign(clientId, timestamp, nonce, client.getAppSecret(), sign, params);
                    return chain.filter(addClientHeader(newExchange, client));
                } catch (Exception e) {
                    return ServletUtils.unauthorized(newExchange.getResponse(), e.getMessage());
                }
            });
        }
    }

    /**
     * 读取查询参数
     *
     * @param request
     * @return
     */
    private Map<String, Object> readQuery(ServerHttpRequest request) {
        Map<String, Object> params = new HashMap<>();
        request.getQueryParams().forEach((key, value) -> {
            if (value.size() == 1) {
                params.put(key, value.get(0));
            } else {
                params.put(key, value);
            }
        });
        return params;
    }

    /**
     * 读取Body，并重新包装Request
     *
     * @param exchange
     * @param handler
     */
    private Mono<Void> readBody(ServerWebExchange exchange, BiFunction<String, ServerWebExchange, Mono<Void>> handler) {
        return DataBufferUtils.join(exchange.getRequest().getBody())
                .flatMap(dataBuffer -> {
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    DataBufferUtils.release(dataBuffer);
                    String body = new String(bytes, StandardCharsets.UTF_8);
                    ServerHttpRequestDecorator decorator =
                            new ServerHttpRequestDecorator(exchange.getRequest()) {
                                @Override
                                public Flux<DataBuffer> getBody() {
                                    return Flux.defer(() ->
                                            Flux.just(exchange.getResponse()
                                                    .bufferFactory()
                                                    .wrap(bytes))
                                    );
                                }
                            };

                    ServerWebExchange newExchange = exchange.mutate().request(decorator).build();
                    return handler.apply(body, newExchange);
                });
    }

    /**
     * 组装Client信息
     *
     * @param exchange
     * @param client
     * @return
     */
    private ServerWebExchange addClientHeader(ServerWebExchange exchange, OpenApiClient client) {
        ServerHttpRequest request =
                exchange.getRequest()
                        .mutate()
                        .header(SecurityConstants.CLIENT_ID_HEADER, client.getClientId())
                        .build();
        return exchange.mutate().request(request).build();
    }

    @Override
    public int getOrder() {
        return -400;
    }
}
