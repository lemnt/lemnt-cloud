package com.lemnt.system.application.dubbo;

import com.lemnt.system.api.RemoteConfigService;
import com.lemnt.system.domain.config.gateway.ConfigGateway;
import com.lemnt.system.domain.config.model.Config;

import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 配置远程服务实现
 *
 * @author wangxx
 */
@Slf4j
@Component
@DubboService
@RequiredArgsConstructor
public class RemoteConfigServiceImpl implements RemoteConfigService {
    private final ConfigGateway configGateway;

    @Override
    public String getConfig(String configKey) {
        Config config = configGateway.selectByKey(configKey);
        return config != null ? config.getConfigValue() : null;
    }
}
