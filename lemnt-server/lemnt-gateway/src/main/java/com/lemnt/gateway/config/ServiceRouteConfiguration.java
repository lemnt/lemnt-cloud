package com.lemnt.gateway.config;

import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertySources;
import org.springframework.cloud.gateway.config.GatewayProperties;
import org.springframework.cloud.gateway.filter.factory.GatewayFilterFactory;
import org.springframework.cloud.gateway.handler.predicate.RoutePredicateFactory;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionRouteLocator;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.cloud.gateway.support.ConfigurationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.io.ClassPathResource;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 前端保留服务前缀，按各服务Controller实际路径转发，网关不依赖业务RPC。
 */
@Configuration
public class ServiceRouteConfiguration {

    /**
     * 标准配置源只打包一份，由框架解析方法和路径，不改写业务前缀、参数或过滤链。
     */
    @Bean
    public RouteLocator commerceRoutes(
        List<RoutePredicateFactory> predicates,
        List<GatewayFilterFactory> filters,
        ConfigurationService configurationService) {
        var yaml = new YamlPropertiesFactoryBean();
        yaml.setResources(new ClassPathResource("commerce-gateway-routes.yaml"));
        var source = new PropertiesPropertySource("commerce-routes", yaml.getObject());
        var definitions =
            new Binder(ConfigurationPropertySources.from(source))
                .bind(
                    "spring.cloud.gateway.server.webflux.routes",
                    Bindable.listOf(RouteDefinition.class))
                .orElseThrow(() -> new IllegalStateException("商城交易端点路由配置缺失"));
        // 保持原DSL语义：动态路由的默认改写过滤器不施加到这些内置端点；全局过滤器照常执行。
        var properties = new GatewayProperties();
        properties.setFailOnRouteDefinitionError(true);
        return new RouteDefinitionRouteLocator(
            () -> Flux.fromIterable(definitions),
            predicates,
            filters,
            properties,
            configurationService);
    }

    /**
     * 会员认证精确路由仍由order=-10的原配置优先处理。
     */
    @Bean
    public RouteLocator serviceRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
            .route(
                "lemnt-member-records",
                r -> r.order(-9).path("/member/admin/**").uri("lb://lemnt-member"))
            .route(
                "lemnt-member-prefixed",
                r ->
                    r.order(-8)
                        .path("/member/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri("lb://lemnt-member"))
            .route(
                "lemnt-system-prefixed",
                r ->
                    r.order(-8)
                        .path("/system/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri("lb://lemnt-system"))
            .route(
                "lemnt-auth-prefixed",
                r ->
                    r.order(-8)
                        .path("/auth/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri("lb://lemnt-auth"))
            .build();
    }
}
