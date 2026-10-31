package com.lemnt.system.application.config.executor;

import com.lemnt.system.domain.config.gateway.ConfigGateway;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 刷新参数缓存命令执行器
 *
 * @author wangxx
 */
@Component
@RequiredArgsConstructor
public class ConfigCacheRefreshCmdExe {
    private final ConfigGateway configGateway;

    /**
     * 刷新参数缓存
     *
     * @return 结果
     */
    public Boolean execute() {
        return configGateway.resetCache();
    }
}
