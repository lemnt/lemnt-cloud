package com.lemnt.gateway.filter;

import com.lemnt.common.core.utils.StringUtils;
import com.lemnt.common.security.constant.SecurityConstants;
import com.lemnt.common.security.constant.TokenConstants;
import com.lemnt.common.security.model.SecurityUser;
import com.lemnt.common.security.service.TokenService;
import com.lemnt.gateway.config.properties.IgnoreWhiteProperties;
import com.lemnt.gateway.utils.ServletUtils;

import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Set;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_ORIGINAL_REQUEST_URL_ATTR;

/**
 * 网关统一鉴权，两端复用公共令牌组件，通过服务端接口路径区分所需身份。
 *
 * @author wangxx
 */
@Component
public class AuthFilter implements GlobalFilter, Ordered {

    private static final Set<String> PUBLIC_APP_POST_PATHS = Set.of(
            "/member/app/auth/login",
            "/member/app/auth/refresh-token",
            "/member/app/auth/logout");

    @Autowired
    private IgnoreWhiteProperties ignoreWhite;
    @Autowired
    private TokenService tokenService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, @NonNull GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = getOriginalPath(exchange, request);
        boolean appRequest = isAppPath(path);
        ServerWebExchange clean = clearIdentityHeaders(exchange);
        // 小程序公开规则单独按完整路径匹配，不受后台宽泛白名单影响。
        if (appRequest ? isPublicAppRequest(request.getMethod(), path)
                : path.startsWith("/open") || StringUtils.matches(path, ignoreWhite.getWhites())) {
            return chain.filter(clean);
        }
        String token = getToken(request);
        if (StringUtils.isEmpty(token)) {
            return ServletUtils.unauthorized(exchange.getResponse(), "令牌不能为空");
        }
        String userType = appRequest ? SecurityConstants.MEMBER_USER_TYPE : SecurityConstants.ADMIN_USER_TYPE;
        SecurityUser loginUser = tokenService.getSecurityUser(token, userType);
        if (loginUser == null || loginUser.getUserId() == null) {
            return ServletUtils.unauthorized(exchange.getResponse(), "登录状态已过期或令牌无效");
        }
        if (appRequest) {
            ServerHttpRequest.Builder builder = clean.getRequest().mutate();
            addHeader(builder, SecurityConstants.HEADER_USER_ID, loginUser.getUserId());
            return chain.filter(clean.mutate().request(builder.build()).build());
        }
        if (StringUtils.isAnyBlank(loginUser.getToken(), loginUser.getUsername())) {
            return ServletUtils.unauthorized(exchange.getResponse(), "令牌验证失败");
        }
        ServerHttpRequest.Builder builder = clean.getRequest().mutate();
        addHeader(builder, SecurityConstants.HEADER_USER_KEY, loginUser.getToken());
        addHeader(builder, SecurityConstants.HEADER_USER_ID, loginUser.getUserId());
        addHeader(builder, SecurityConstants.HEADER_USER_NAME, loginUser.getUsername());
        String roleKey = loginUser.getRoles() == null
                ? loginUser.getRoleKey() : String.join(",", loginUser.getRoles());
        addHeader(builder, SecurityConstants.HEADER_ROLE_KEY, roleKey);
        return chain.filter(clean.mutate().request(builder.build()).build());
    }

    /** 通过完整接口路径选择会员身份域，不匹配近似前缀。 */
    private boolean isAppPath(String path) {
        return path.equals("/files/app/upload") || path.equals("/member/app") || path.startsWith("/member/app/")
                || path.equals("/mall/app") || path.startsWith("/mall/app/")
                || path.equals("/trade/app") || path.startsWith("/trade/app/");
    }

    /** 保留已有小程序公开读接口、登录入口。 */
    private boolean isPublicAppRequest(HttpMethod method, String path) {
        if (method == HttpMethod.POST) {
            return PUBLIC_APP_POST_PATHS.contains(path);
        }
        return method == HttpMethod.GET && (path.equals("/mall/app/comments")
                || path.equals("/mall/app/categories")
                || path.equals("/mall/app/products")
                || path.matches("/mall/app/products/[1-9][0-9]{0,18}"));
    }

    /** 清除客户端身份头，身份只从已校验的公共会话写入。 */
    private ServerWebExchange clearIdentityHeaders(ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest().mutate().headers(headers -> {
            for (String header : Set.of(SecurityConstants.HEADER_USER_ID, SecurityConstants.HEADER_USER_KEY,
                    SecurityConstants.HEADER_USER_NAME, SecurityConstants.HEADER_ROLE_KEY,
                    SecurityConstants.HEADER_ROLE_PERMISSION, SecurityConstants.HEADER_LOGIN_USER,
                    "X-Lemnt-Login-Ip", "X-Member-Id", "memberId")) {
                headers.remove(header);
            }
            // 仅使用连接对端，不信任客户端转发头；代理链需要另行配置。
            var remote=exchange.getRequest().getRemoteAddress();
            if(remote != null && remote.getAddress() != null) {
                headers.set("X-Lemnt-Login-Ip",remote.getAddress().getHostAddress().split("%",2)[0]);
            }
        }).build();
        return exchange.mutate().request(request).build();
    }

    /** 写入经过编码的可信身份信息。 */
    private void addHeader(ServerHttpRequest.Builder builder, String name, Object value) {
        if (value != null) {
            builder.header(name, ServletUtils.urlEncode(value.toString()));
        }
    }

    /** 与后台保持一致，提取公共访问令牌。 */
    private String getToken(ServerHttpRequest request) {
        String token = request.getHeaders().getFirst(SecurityConstants.AUTHORIZATION_HEADER);
        if (StringUtils.isNotBlank(token) && token.startsWith(TokenConstants.PREFIX)) {
            return token.substring(TokenConstants.PREFIX.length());
        }
        return token;
    }

    /** StripPrefix 执行后仍使用进入网关时的完整路径。 */
    private String getOriginalPath(ServerWebExchange exchange, ServerHttpRequest request) {
        Set<URI> originalUrls = exchange.getAttribute(GATEWAY_ORIGINAL_REQUEST_URL_ATTR);
        return originalUrls == null || originalUrls.isEmpty()
                ? request.getURI().getPath() : originalUrls.iterator().next().getPath();
    }

    @Override
    public int getOrder() {
        return 2;
    }
}
