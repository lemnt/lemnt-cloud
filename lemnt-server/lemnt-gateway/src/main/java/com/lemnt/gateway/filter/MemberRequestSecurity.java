package com.lemnt.gateway.filter;

import com.lemnt.gateway.service.MemberSessionLookup;
import com.lemnt.gateway.utils.ServletUtils;
import com.lemnt.common.security.constant.SecurityConstants;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.http.HttpMethod;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import java.util.Set;

@org.springframework.stereotype.Component
@lombok.RequiredArgsConstructor
public class MemberRequestSecurity {
    private final MemberSessionLookup sessions;
    private static final Set<String> PUBLIC_POST =
            Set.of(
                    "/member/app/auth/login",
                    "/member/app/auth/refresh-token",
                    "/member/app/auth/logout",
                    "/member/app/auth/send-sms-code",
                    "/member/app/auth/validate-sms-code",
                    "/member/app/auth/sms-login",
                    "/member/app/auth/social-state",
                    "/member/app/auth/social-login",
                    "/member/app/auth/weixin-mini-app-login",
                    "/member/app/auth/create-weixin-jsapi-signature");

    public static boolean memberPath(String path) {
        return path.equals("/mall/app")
                || path.startsWith("/mall/app/")
                || path.equals("/member/app")
                || path.startsWith("/member/app/")
                || path.equals("/trade/app")
                || path.startsWith("/trade/app/");
    }

    private static final class AuthorityUnavailable extends RuntimeException {
        AuthorityUnavailable(Throwable cause) {
            super(cause);
        }
    }

    public Mono<Void> filter(ServerWebExchange exchange, String path, GatewayFilterChain chain) {
        var request =
                exchange.getRequest()
                        .mutate()
                        .headers(
                                headers -> {
                                    for (String header :
                                            Set.of(
                                                    SecurityConstants.HEADER_USER_ID,
                                                    SecurityConstants.HEADER_USER_KEY,
                                                    SecurityConstants.HEADER_USER_NAME,
                                                    SecurityConstants.HEADER_ROLE_KEY,
                                                    SecurityConstants.HEADER_ROLE_PERMISSION,
                                                    SecurityConstants.HEADER_LOGIN_USER,
                                                    "X-Member-Id",
                                                    "memberId")) headers.remove(header);
                                })
                        .build();
        var clean = exchange.mutate().request(request).build();
        if ((request.getMethod() == HttpMethod.GET
                        && (path.equals("/mall/app/categories")
                                || path.equals("/mall/app/products")
                                || path.matches("/mall/app/products/[1-9][0-9]{0,18}")))
                || (request.getMethod() == HttpMethod.POST && PUBLIC_POST.contains(path))
                || (request.getMethod() == HttpMethod.PUT
                        && path.equals("/member/app/user/reset-password"))
                || (request.getMethod() == HttpMethod.GET
                        && path.equals("/member/app/auth/social-auth-redirect")))
            return chain.filter(clean);
        String header = request.getHeaders().getFirst("Authorization");
        if (header == null || !header.matches("Bearer mbr_a_[A-Za-z0-9_-]{43}"))
            return ServletUtils.unauthorized(exchange.getResponse(), "会员会话无效");
        String token = header.substring(7);
        return Mono.fromCallable(
                        () -> {
                            Long id = sessions.authenticate(token);
                            return id != null && id > 0;
                        })
                .subscribeOn(Schedulers.boundedElastic())
                .timeout(java.time.Duration.ofSeconds(4))
                .onErrorMap(AuthorityUnavailable::new)
                .flatMap(
                        valid ->
                                valid
                                        ? chain.filter(clean)
                                        : ServletUtils.unauthorized(
                                                exchange.getResponse(), "会员会话无效"))
                .onErrorResume(
                        AuthorityUnavailable.class,
                        ex -> ServletUtils.error(exchange.getResponse(), 503, "会员认证服务暂不可用"));
    }
}
