package com.lemnt.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 三个会员认证入口归属 认证；保留原路径及其余会员路由。
 */
@Configuration
public class MemberAuthRouteConfiguration {
    /**
     * 优先于常规 会员 路由匹配，不捕获短信和第三方身份核验入口。
     */
    @Bean
    public RouteLocator memberAuthRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
            .route("lemnt-member-auth", route -> route.order(-10)
                .path("/member/app/auth/login", "/member/app/auth/refresh-token", "/member/app/auth/logout")
                .uri("lb://lemnt-auth"))
            .build();
    }
}
