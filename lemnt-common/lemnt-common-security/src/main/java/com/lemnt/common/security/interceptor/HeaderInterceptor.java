package com.lemnt.common.security.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.lemnt.common.security.constant.SecurityConstants;
import com.lemnt.common.security.context.SecurityContextHolder;
import com.lemnt.common.security.model.SecurityUser;
import org.slf4j.MDC;
import org.springframework.lang.NonNull;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.AsyncHandlerInterceptor;
import com.lemnt.common.core.utils.ServletUtils;
import com.lemnt.common.core.utils.StringUtils;
import com.lemnt.common.security.auth.AuthUtil;
import com.lemnt.common.security.utils.SecurityUtils;

import java.util.Objects;
import java.util.UUID;

/**
 * 自定义请求头拦截器，将Header数据封装到线程变量中方便获取
 * 注意：此拦截器会同时验证当前用户有效期自动刷新有效期
 *
 * @author wangxx
 */
public class HeaderInterceptor implements AsyncHandlerInterceptor {

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        // 用户上下文
        SecurityContextHolder.setUserId(ServletUtils.getHeader(request, SecurityConstants.HEADER_USER_ID));
        SecurityContextHolder.setUserName(ServletUtils.getHeader(request, SecurityConstants.HEADER_USER_NAME));
        String userKey = ServletUtils.getHeader(request, SecurityConstants.HEADER_USER_KEY);
        SecurityContextHolder.setUserKey(userKey);

        // TraceId 处理
        String traceId = ServletUtils.getHeader(request, SecurityConstants.HEADER_TRACE_ID);
        if (StringUtils.isNotBlank(traceId)) {
            MDC.put(SecurityConstants.TRACE_ID, traceId);
        }
        if (StringUtils.isNotBlank(userKey)) {
            SecurityUser loginUser = AuthUtil.getLoginUserByUserKey(userKey);
            if (Objects.nonNull(loginUser)) {
                AuthUtil.verifyLoginUserExpire(loginUser);
                SecurityContextHolder.set(SecurityConstants.LOGIN_USER, loginUser);
            }
        }
        return true;
    }

    @Override
    public void afterConcurrentHandlingStarted(@NonNull HttpServletRequest request,
                                               @NonNull HttpServletResponse response,
                                               @NonNull Object handler) {
        clearContext();
    }

    @Override
    public void afterCompletion(@NonNull HttpServletRequest request,
                                @NonNull HttpServletResponse response,
                                @NonNull Object handler,
                                Exception ex) {
        clearContext();
    }

    /**
     * 清理线程变量
     */
    private void clearContext() {
        MDC.remove(SecurityConstants.TRACE_ID);
        SecurityContextHolder.remove();
    }
}
