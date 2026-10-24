package com.lemnt.gateway.filter;

import java.nio.charset.StandardCharsets;
import java.util.function.BiFunction;

import com.lemnt.gateway.utils.ServletUtils;
import com.alibaba.fastjson2.JSONObject;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.Strings;
import org.jspecify.annotations.NonNull;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.stereotype.Component;
import com.lemnt.common.core.utils.JsonUtils;
import com.lemnt.gateway.config.properties.CaptchaProperties;
import com.lemnt.gateway.service.ValidateCodeService;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 验证码过滤器
 *
 * @author wangxx
 */
@Component
public class CaptchaFilter implements GlobalFilter, Ordered {
    private final static String[] VALIDATE_URL = new String[]{"/auth/sessions/login"};

    @Resource
    private ValidateCodeService validateCodeService;
    @Resource
    private CaptchaProperties captchaProperties;

    private static final String CODE = "code";

    private static final String UUID = "uuid";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, @NonNull GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        if (!captchaProperties.getEnabled()) {
            return chain.filter(exchange);
        }
        if (!Strings.CS.equalsAny(request.getURI().getPath(), VALIDATE_URL)) {
            return chain.filter(exchange);
        }
        return readBody(exchange, (body, newExchange) -> {
            try {
                JSONObject obj = JsonUtils.toJsonObj(body);
                validateCodeService.checkCaptcha(obj.getString(CODE), obj.getString(UUID));
                return chain.filter(newExchange);
            } catch (Exception e) {
                return ServletUtils.unauthorized(exchange.getResponse(), "验证码错误");
            }
        });
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

                                @Override
                                public HttpHeaders getHeaders() {
                                    HttpHeaders headers = new HttpHeaders();
                                    headers.putAll(super.getHeaders());
                                    headers.remove(HttpHeaders.CONTENT_LENGTH);
                                    headers.setContentLength(bytes.length);
                                    return headers;
                                }
                            };

                    ServerWebExchange newExchange = exchange.mutate().request(decorator).build();
                    return handler.apply(body, newExchange);
                });
    }

    @Override
    public int getOrder() {
        return -350;
    }
}
