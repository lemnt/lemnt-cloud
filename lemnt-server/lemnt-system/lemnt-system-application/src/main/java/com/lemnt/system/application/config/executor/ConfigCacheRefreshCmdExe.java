package com.lemnt.system.application.config.executor;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import com.lemnt.system.domain.config.gateway.ConfigGateway;

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
