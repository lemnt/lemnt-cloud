package com.lemnt.system.application.log.executor;

import com.lemnt.system.domain.log.gateway.LogininforGateway;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 清空登录日志命令执行器
 *
 * @author wangxx
 */
@Component
@RequiredArgsConstructor
public class LogininforCleanCmdExe {
    private final LogininforGateway logininforGateway;

    /**
     * 执行清空登录日志
     */
    public Boolean execute() {
        logininforGateway.clean();
        return true;
    }
}
