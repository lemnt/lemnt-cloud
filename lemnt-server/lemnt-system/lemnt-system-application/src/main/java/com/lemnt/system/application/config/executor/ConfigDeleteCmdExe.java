package com.lemnt.system.application.config.executor;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import com.lemnt.system.domain.config.gateway.ConfigGateway;

import java.util.List;

/**
 * 删除参数配置命令执行器
 *
 * @author wangxx
 */
@Component
@RequiredArgsConstructor
public class ConfigDeleteCmdExe {
    private final ConfigGateway configGateway;

    /**
     * 执行删除
     *
     * @param configIds 参数ID数组
     * @return 行数
     */
    public Boolean execute(List<Long> configIds) {
        return configGateway.deleteByIds(configIds);
    }
}
