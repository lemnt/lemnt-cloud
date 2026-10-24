package com.lemnt.gateway.handler;

import com.lemnt.gateway.utils.ServletUtils;
import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.GatewayCallbackManager;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;

/**
 * 自定义限流异常处理
 *
 * @author wangxx
 */
public class SentinelFallbackHandler implements WebExceptionHandler {

    private Mono<Void> writeResponse(ServerResponse response, ServerWebExchange exchange) {
        return ServletUtils.error(exchange.getResponse(), HttpStatus.TOO_MANY_REQUESTS.value(),
                "请求超过最大数，请稍候再试");
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, @NonNull Throwable ex) {
        if (exchange.getResponse().isCommitted()) {
            return Mono.error(ex);
        }
        if (!BlockException.isBlockException(ex)) {
            return Mono.error(ex);
        }
        return handleBlockedRequest(exchange, ex).flatMap(response -> writeResponse(response, exchange));
    }

    private Mono<ServerResponse> handleBlockedRequest(ServerWebExchange exchange, Throwable throwable) {
        return GatewayCallbackManager.getBlockHandler().handleRequest(exchange, throwable);
    }
}
