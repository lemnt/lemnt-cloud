package com.lemnt.system.application.config.dto;

import lombok.Data;

/**
 * 字典类型 DTO
 *
 * @author wangxx
 */
@Data
public class DictTypeDTO {
    /**
     * 字典主键
     */
    private Long dictId;

    /**
     * 字典名称
     */
    private String dictName;

    /**
     * 字典类型
     */
    private String dictType;

    /**
     * 状态（1正常 2停用）
     */
    private String status;

    /**
     * 备注
     */
    private String remark;
}
