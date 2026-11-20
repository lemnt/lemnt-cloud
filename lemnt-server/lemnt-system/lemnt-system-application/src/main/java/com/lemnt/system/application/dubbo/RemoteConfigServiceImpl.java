package com.lemnt.system.application.dubbo;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.system.api.BusinessConfigKeys;
import com.lemnt.system.api.RemoteConfigService;
import com.lemnt.system.api.bo.BusinessConfigSaveBo;
import com.lemnt.system.domain.config.gateway.ConfigGateway;
import com.lemnt.system.domain.config.model.Config;

import com.alibaba.fastjson2.JSON;
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
    public void saveBusinessConfig(BusinessConfigSaveBo value) {
        if (value == null
            || !(BusinessConfigKeys.MEMBER_POINT.equals(value.configKey())
            || BusinessConfigKeys.TRADE.equals(value.configKey()))
            || !JSON.isValidObject(value.configValue())) {
            throw new ServiceException("业务配置键或JSON对象不合法", 400);
        }
        var config = new Config();
        config.setConfigKey(value.configKey());
        config.setConfigName(
            BusinessConfigKeys.MEMBER_POINT.equals(value.configKey()) ? "积分交易配置" : "交易基础配置");
        config.setConfigValue(value.configValue());
        config.setUpdateBy(value.operator());
        configGateway.saveBusinessConfig(config);
    }

    @Override
    public String getConfig(String configKey) {
        Config config = configGateway.selectByKey(configKey);
        return config != null ? config.getConfigValue() : null;
    }
}
