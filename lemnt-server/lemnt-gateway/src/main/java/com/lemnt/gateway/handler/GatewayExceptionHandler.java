package com.lemnt.gateway.handler;

import com.lemnt.gateway.utils.ServletUtils;

import org.springframework.cloud.gateway.support.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.webflux.error.ErrorWebExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.io.IOException;

/**
 * 网关统一异常处理
 *
 * @author wangxx
 */
@Order(-1)
@Configuration
public class GatewayExceptionHandler implements ErrorWebExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GatewayExceptionHandler.class);

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();

        if (exchange.getResponse().isCommitted()) {
            return Mono.error(ex);
        }

        int code;
        String msg;

        if (ex instanceof NotFoundException) {
            code = HttpStatus.SERVICE_UNAVAILABLE.value();
            msg = "服务暂不可用，请稍后重试";
        } else if (ex instanceof ResponseStatusException responseStatusException) {
            code = responseStatusException.getStatusCode().value();
            msg = responseStatusException.getReason();
            if (msg == null) {
                msg = "请求失败";
            }
        } else if (ex instanceof IOException) {
            code = HttpStatus.BAD_GATEWAY.value();
            msg = "后端服务连接中断，请重试";
        } else {
            code = HttpStatus.INTERNAL_SERVER_ERROR.value();
            msg = "内部服务器错误";
        }
        log.error("[网关异常处理]请求路径:{},异常信息:{}", exchange.getRequest().getPath(), ex.getMessage());
        return ServletUtils.error(response, code, msg);
    }
}
