package com.lemnt.system.application.config.cmd;

import java.io.Serializable;

/**
 * 修改参数配置命令
 *
 * @author wangxx
 */
public record ConfigUpdateCmd(
        /**
         * 参数主键
         */
        Long configId,
        /**
         * 参数名称
         */
        String configName,
        /**
         * 参数键名
         */
        String configKey,
        /**
         * 参数键值
         */
        String configValue,
        /**
         * 系统内置（1是 0否）
         */
        String configType,
        /**
         * 备注
         */
        String remark
) implements Serializable {
}
