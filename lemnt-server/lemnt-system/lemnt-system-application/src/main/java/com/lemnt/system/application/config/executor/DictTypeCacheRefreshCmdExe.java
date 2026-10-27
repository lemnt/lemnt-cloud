package com.lemnt.system.application.config.executor;

import com.lemnt.system.domain.config.gateway.DictDataGateway;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 刷新缓存字典类型命令执行器
 *
 * @author wangxx
 */
@Component
@RequiredArgsConstructor
public class DictTypeCacheRefreshCmdExe {
    private final DictDataGateway dictDataGateway;
    public Boolean execute() {
        dictDataGateway.resetCache();
        return Boolean.TRUE;
    }
}
