package com.lemnt.gateway.filter;

import com.lemnt.common.redis.service.RedisStringService;
import com.lemnt.common.security.constant.SecurityConstants;
import com.lemnt.common.security.constant.TokenConstants;
import com.lemnt.common.security.utils.JwtUtils;
import com.lemnt.gateway.utils.ServletUtils;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import com.lemnt.common.core.constant.CacheConstants;
import com.lemnt.common.core.utils.StringUtils;
import com.lemnt.gateway.config.properties.IgnoreWhiteProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Set;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_ORIGINAL_REQUEST_URL_ATTR;

/**
 * 网关鉴权
 *
 * @author wangxx
 */
@Component
public class AuthFilter implements GlobalFilter, Ordered {
    // 排除过滤的 uri 地址，nacos自行添加
    @Autowired
    private IgnoreWhiteProperties ignoreWhite;
    @Autowired
    private RedisStringService redisStringService;
    @Autowired
    private JwtUtils jwtUtils;

    private static final String OPEN_API_PREFIX = "/open";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, @NonNull GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = getOriginalPath(exchange, request);
        // 开放平台路径跳过
        if (path.startsWith(OPEN_API_PREFIX)) {
            return chain.filter(exchange);
        }
        // 跳过不需要验证的路径
        if (StringUtils.matches(path, ignoreWhite.getWhites())) {
            return chain.filter(exchange);
        }
        String token = getToken(request);
        if (StringUtils.isEmpty(token)) {
            return ServletUtils.unauthorized(exchange.getResponse(), "令牌不能为空");
        }
        Claims claims;
        try {
            claims = jwtUtils.parseToken(token);
        } catch (JwtException | IllegalArgumentException e) {
            // 令牌被篡改、已过期或格式非法时按未授权处理，避免网关抛出 500
            return ServletUtils.unauthorized(exchange.getResponse(), "令牌已过期或验证不正确！");
        }
        String userKey = jwtUtils.getUserKey(claims);
        if (!isLogin(userKey)) {
            return ServletUtils.unauthorized(exchange.getResponse(), "登录状态已过期");
        }
        String userId = jwtUtils.getUserId(claims);
        String username = jwtUtils.getUserName(claims);
        if (StringUtils.isAnyBlank(userId, username)) {
            return ServletUtils.unauthorized(exchange.getResponse(), "令牌验证失败");
        }

        ServerHttpRequest.Builder builder = request.mutate();
        // 设置用户信息到请求
        addHeader(builder, SecurityConstants.HEADER_USER_KEY, userKey);
        addHeader(builder, SecurityConstants.HEADER_USER_ID, userId);
        addHeader(builder, SecurityConstants.HEADER_USER_NAME, username);
        addHeader(builder, SecurityConstants.HEADER_ROLE_KEY, jwtUtils.getRoleKey(claims));
        return chain.filter(exchange.mutate().request(builder.build()).build());
    }

    /**
     * 是否登录
     */
    private boolean isLogin(String userKey) {
        return redisStringService.hasKey(CacheConstants.LOGIN_TOKEN_KEY + userKey);
    }

    /**
     * 添加请求头
     *
     * @param mutate
     * @param name
     * @param value
     */
    private void addHeader(ServerHttpRequest.Builder mutate, String name, Object value) {
        if (value == null) {
            return;
        }
        String valueStr = value.toString();
        String valueEncode = ServletUtils.urlEncode(valueStr);
        mutate.header(name, valueEncode);
    }

    /**
     * 获取请求token
     */
    private String getToken(ServerHttpRequest request) {
        String token = request.getHeaders().getFirst(SecurityConstants.AUTHORIZATION_HEADER);
        // 如果前端设置了令牌前缀，则裁剪掉前缀
        if (StringUtils.isNotBlank(token) && token.startsWith(TokenConstants.PREFIX)) {
            token = token.replaceFirst(TokenConstants.PREFIX, StringUtils.EMPTY);
        }
        return token;
    }

    /**
     * StripPrefix 执行后仍使用进入网关时的完整路径匹配白名单。
     */
    private String getOriginalPath(ServerWebExchange exchange, ServerHttpRequest request) {
        Set<URI> originalUrls = exchange.getAttribute(GATEWAY_ORIGINAL_REQUEST_URL_ATTR);
        if (originalUrls == null || originalUrls.isEmpty()) {
            return request.getURI().getPath();
        }
        return originalUrls.iterator().next().getPath();
    }

    @Override
    public int getOrder() {
        return 2;
    }
}
