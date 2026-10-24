package com.lemnt.system.application.config.executor;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.common.security.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import com.lemnt.system.application.config.cmd.ConfigAddCmd;
import com.lemnt.system.domain.config.model.Config;
import com.lemnt.system.domain.config.gateway.ConfigGateway;

/**
 * 新增参数配置命令执行器
 *
 * @author wangxx
 */
@Component
@RequiredArgsConstructor
public class ConfigAddCmdExe {
    private final ConfigGateway configGateway;

    /**
     * 执行新增
     *
     * @param cmd 命令
     * @return 行数
     */
    public Boolean execute(ConfigAddCmd cmd) {
        if (configGateway.checkConfigKeyUnique(cmd.configKey()) != null) {
            throw new ServiceException("新增参数'" + cmd.configName() + "'失败，参数键名已存在");
        }
        Config entity = new Config();
        entity.setConfigName(cmd.configName());
        entity.setConfigKey(cmd.configKey());
        entity.setConfigValue(cmd.configValue());
        entity.setConfigType(cmd.configType());
        entity.setRemark(cmd.remark());
        entity.setCreateBy(SecurityUtils.getUsername());
        return configGateway.insert(entity);
    }
}
