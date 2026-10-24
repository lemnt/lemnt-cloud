package com.lemnt.system.application.config.executor;

import com.lemnt.system.domain.config.gateway.DictTypeGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 刷新缓存字典类型命令执行器
 *
 * @author wangxx
 */
@Component
@RequiredArgsConstructor
public class DictTypeCacheRefreshCmdExe {
    private final DictTypeGateway dictTypeGateway;

    public Boolean execute() {
        dictTypeGateway.resetCache();
        return Boolean.TRUE;
    }
}
