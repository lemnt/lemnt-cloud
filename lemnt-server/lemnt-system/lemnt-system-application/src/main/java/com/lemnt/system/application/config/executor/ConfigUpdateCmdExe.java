package com.lemnt.system.application.config.executor;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.common.security.utils.SecurityUtils;
import com.lemnt.system.application.config.cmd.ConfigUpdateCmd;
import com.lemnt.system.domain.config.gateway.ConfigGateway;
import com.lemnt.system.domain.config.model.Config;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 修改参数配置命令执行器
 *
 * @author wangxx
 */
@Component
@RequiredArgsConstructor
public class ConfigUpdateCmdExe {
    private final ConfigGateway configGateway;

    /**
     * 执行修改
     *
     * @param cmd 命令
     * @return 行数
     */
    public Boolean execute(ConfigUpdateCmd cmd) {
        if (configGateway.checkConfigKeyUnique(cmd.configKey(), cmd.configId()) != null) {
            throw new ServiceException("修改参数'" + cmd.configName() + "'失败，参数键名已存在");
        }
        Config entity = new Config();
        entity.setConfigId(cmd.configId());
        entity.setConfigName(cmd.configName());
        entity.setConfigKey(cmd.configKey());
        entity.setConfigValue(cmd.configValue());
        entity.setConfigType(cmd.configType());

        // 完整编辑表单中的空备注需要清除数据库旧值。
        entity.setRemark(cmd.remark() == null ? "" : cmd.remark());
        entity.setUpdateBy(SecurityUtils.getUsername());
        return configGateway.update(entity);
    }
}
