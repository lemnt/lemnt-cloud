package com.lemnt.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** 前端保留服务前缀，按各服务Controller实际路径转发，网关不依赖业务RPC。 */
@Configuration
public class ServiceRouteConfiguration {

    /** 会员认证精确路由仍由order=-10的原配置优先处理。 */
    @Bean
    public RouteLocator serviceRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("lemnt-member-records", r -> r.order(-9).path("/member/admin/**")
                        .uri("lb://lemnt-member"))
                .route("lemnt-member-prefixed", r -> r.order(-8).path("/member/**")
                        .filters(f -> f.stripPrefix(1)).uri("lb://lemnt-member"))
                .route("lemnt-system-prefixed", r -> r.order(-8).path("/system/**")
                        .filters(f -> f.stripPrefix(1)).uri("lb://lemnt-system"))
                .route("lemnt-auth-prefixed", r -> r.order(-8).path("/auth/**")
                        .filters(f -> f.stripPrefix(1)).uri("lb://lemnt-auth"))
                .route("lemnt-mall-prefixed", r -> r.order(-8).path("/mall/**")
                        .uri("lb://lemnt-mall"))
                .route("lemnt-trade-prefixed", r -> r.order(-8).path("/trade/**")
                        .uri("lb://lemnt-trade"))
                .build();
    }
}
