package com.lemnt.gateway.handler;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.exception.CaptchaException;
import com.lemnt.gateway.service.ValidateCodeService;

import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.Map;

/**
 * 验证码获取
 *
 * @author wangxx
 */
@Component
public class ValidateCodeHandler implements HandlerFunction<ServerResponse> {

    @Autowired private ValidateCodeService validateCodeService;

    @Override
    public Mono<ServerResponse> handle(@NonNull ServerRequest serverRequest) {
        R<Map<String, Object>> ajax;
        try {
            ajax = validateCodeService.createCaptcha();
        } catch (CaptchaException | IOException e) {
            return Mono.error(e);
        }
        return ServerResponse.status(HttpStatus.OK).body(BodyInserters.fromValue(ajax));
    }
}
