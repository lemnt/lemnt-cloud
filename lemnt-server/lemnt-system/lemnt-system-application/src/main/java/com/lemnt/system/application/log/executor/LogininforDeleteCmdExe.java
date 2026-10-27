package com.lemnt.system.application.log.executor;

import com.lemnt.system.domain.log.gateway.LogininforGateway;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * 批量删除登录日志命令执行器
 *
 * @author wangxx
 */
@Component
@RequiredArgsConstructor
public class LogininforDeleteCmdExe {
    private final LogininforGateway logininforGateway;

    /**
     * 执行批量删除登录日志
     *
     * @param infoIds 登录日志ID数组
     * @return 影响行数
     */
    public Boolean execute(List<Long> infoIds) {
        return logininforGateway.deleteByIds(infoIds);
    }
}
