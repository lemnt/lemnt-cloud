package com.lemnt.common.dubbo.filter;

import cn.hutool.core.convert.Convert;
import com.lemnt.common.core.utils.JsonUtils;
import com.lemnt.common.security.constant.SecurityConstants;
import com.lemnt.common.security.context.SecurityContextHolder;
import com.lemnt.common.security.model.SecurityUser;
import com.lemnt.common.security.utils.SecurityUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.*;
import org.slf4j.MDC;

import java.util.Objects;
import java.util.UUID;

/**
 * Dubbo TraceId 透传 Filter
 *
 * @author wangxx
 * @version 1.0.0
 * @date 2025/7/26 21:44
 */
@Activate(group = {CommonConstants.CONSUMER, CommonConstants.PROVIDER})
public class DubboTraceIdFilter implements Filter {

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        RpcServiceContext serviceContext = RpcContext.getServiceContext();
        RpcContextAttachment clientContext = RpcContext.getClientAttachment();

        boolean isProvider = serviceContext.isProviderSide();
        boolean isConsumer = clientContext.isConsumerSide();
        try {
            if (isConsumer) {
                this.handleConsumer(clientContext);
            } else if (isProvider) {
                this.handleProvider(serviceContext);
            }
            return invoker.invoke(invocation);
        } finally {
            if (isProvider) {
                // 清理避免线程复用时污染
                MDC.remove(SecurityConstants.TRACE_ID);
                // 清理用户线程变量
                SecurityContextHolder.remove();
            }
        }
    }

    /**
     * Consumer 侧处理
     */
    private void handleConsumer(RpcContextAttachment attachment) {
        // 消费者：从 MDC 获取 traceId，若无则新建一个
        String traceId = MDC.get(SecurityConstants.TRACE_ID);
        if (StringUtils.isBlank(traceId)) {
            traceId = UUID.randomUUID().toString().replace("-", "");
            MDC.put(SecurityConstants.TRACE_ID, traceId);
        }
        // 设置到 RpcContext，传递给服务提供方
        attachment.setAttachment(SecurityConstants.HEADER_TRACE_ID, traceId);
        // 设置用户信息到 RpcContext，传递给服务提供方
        attachment.setAttachment(SecurityConstants.HEADER_USER_ID, Convert.toStr(SecurityUtils.getUserId(), StringUtils.EMPTY));
        attachment.setAttachment(SecurityConstants.HEADER_USER_NAME, Convert.toStr(SecurityUtils.getUsername(), StringUtils.EMPTY));
        attachment.setAttachment(SecurityConstants.HEADER_ROLE_PERMISSION, Convert.toStr(SecurityContextHolder.getPermission(), StringUtils.EMPTY));
        attachment.setAttachment(SecurityConstants.HEADER_ROLE_KEY, Convert.toStr(String.join(",", SecurityUtils.getRoleKey()), StringUtils.EMPTY));
        if (Objects.isNull(SecurityUtils.getLoginUser())) {
            attachment.setAttachment(SecurityConstants.HEADER_LOGIN_USER, StringUtils.EMPTY);
        } else {
            attachment.setAttachment(SecurityConstants.HEADER_LOGIN_USER, Convert.toStr(JsonUtils.toJsonStr(SecurityUtils.getLoginUser()), StringUtils.EMPTY));
        }
    }

    /**
     * Provider 侧处理
     */
    private void handleProvider(RpcServiceContext attachment) {
        // 提供者：从 RpcContext 获取 traceId，放入 MDC
        String traceId = attachment.getAttachment(SecurityConstants.HEADER_TRACE_ID);
        if (StringUtils.isBlank(traceId)) {
            traceId = UUID.randomUUID().toString().replace("-", "");
        }
        MDC.put(SecurityConstants.TRACE_ID, traceId);
        // 提供者：从 RpcContext 获取 用户信息，放入本地线程
        String userId = attachment.getAttachment(SecurityConstants.HEADER_USER_ID);
        if (StringUtils.isNotBlank(userId)) {
            SecurityContextHolder.setUserId(userId);
        }
        String userName = attachment.getAttachment(SecurityConstants.HEADER_USER_NAME);
        if (StringUtils.isNotBlank(userName)) {
            SecurityContextHolder.setUserName(userName);
        }
        String permission = attachment.getAttachment(SecurityConstants.HEADER_ROLE_PERMISSION);
        if (StringUtils.isNotBlank(permission)) {
            SecurityContextHolder.setPermission(permission);
        }
        String roleKey = attachment.getAttachment(SecurityConstants.HEADER_ROLE_KEY);
        if (StringUtils.isNotBlank(roleKey)) {
            SecurityContextHolder.setRoleKey(roleKey);
        }
        String loginUser = attachment.getAttachment(SecurityConstants.HEADER_LOGIN_USER);
        if (StringUtils.isNotBlank(loginUser)) {
            SecurityContextHolder.set(SecurityConstants.LOGIN_USER, JsonUtils.toBean(loginUser, SecurityUser.class));
        }
    }
}
