package com.lemnt.system.application.config.executor;

import com.lemnt.system.application.config.converter.ConfigConverter;
import com.lemnt.system.application.config.dto.ConfigDTO;
import com.lemnt.system.domain.config.gateway.ConfigGateway;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 参数配置详情查询执行器
 *
 * @author wangxx
 */
@Component
@RequiredArgsConstructor
public class ConfigDetailQueryExe {
    private final ConfigGateway configGateway;

    /**
     * 按 ID 查询详情
     *
     * @param configId 参数ID
     * @return DTO
     */
    public ConfigDTO executeById(Long configId) {
        return ConfigConverter.toDTO(configGateway.selectById(configId));
    }

    /**
     * 按键名查询参数值
     *
     * @param configKey 参数键名
     * @return DTO
     */
    public ConfigDTO executeByKey(String configKey) {
        return ConfigConverter.toDTO(configGateway.selectByKey(configKey));
    }
}
