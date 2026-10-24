package com.lemnt.system.application.config.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 参数配置 DTO
 *
 * @author wangxx
 */
@Data
public class ConfigDTO {
    /**
     * 参数主键
     */
    private Long configId;

    /**
     * 参数名称
     */
    private String configName;

    /**
     * 参数键名
     */
    private String configKey;

    /**
     * 参数键值
     */
    private String configValue;

    /**
     * 系统内置（1是 0否）
     */
    private String configType;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
