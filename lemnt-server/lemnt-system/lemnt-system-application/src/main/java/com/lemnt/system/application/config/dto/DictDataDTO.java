package com.lemnt.system.application.config.dto;

import lombok.Data;

/**
 * 字典数据 DTO
 *
 * @author wangxx
 */
@Data
public class DictDataDTO {
    /**
     * 字典编码
     */
    private Long dictCode;

    /**
     * 字典排序
     */
    private Long dictSort;

    /**
     * 字典标签
     */
    private String dictLabel;

    /**
     * 字典键值
     */
    private String dictValue;

    /**
     * 字典类型
     */
    private String dictType;

    /**
     * 样式属性
     */
    private String cssClass;

    /**
     * 表格字典样式
     */
    private String listClass;

    /**
     * 是否默认（1是 0否）
     */
    private String isDefault;

    /**
     * 状态（1正常 2停用）
     */
    private String status;

    /**
     * 备注
     */
    private String remark;
}
