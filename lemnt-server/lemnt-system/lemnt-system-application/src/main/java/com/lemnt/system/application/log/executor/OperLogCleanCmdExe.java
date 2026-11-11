package com.lemnt.system.application.log.executor;

import com.lemnt.system.domain.log.gateway.OperLogGateway;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 清空操作日志命令执行器
 *
 * @author wangxx
 */
@Component
@RequiredArgsConstructor
public class OperLogCleanCmdExe {
    private final OperLogGateway operLogGateway;

    /**
     * 执行清空操作日志
     */
    public Boolean execute() {
        operLogGateway.clean();
        return true;
    }
}
