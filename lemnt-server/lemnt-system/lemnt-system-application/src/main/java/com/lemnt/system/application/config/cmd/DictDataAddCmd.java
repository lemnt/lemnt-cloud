package com.lemnt.system.application.config.cmd;

import java.io.Serializable;

/**
 * 新增字典数据命令
 *
 * @author wangxx
 */
public record DictDataAddCmd(
        /**
         * 字典排序
         */
        Long dictSort,
        /**
         * 字典标签
         */
        String dictLabel,
        /**
         * 字典键值
         */
        String dictValue,
        /**
         * 字典类型
         */
        String dictType,
        /**
         * 样式属性
         */
        String cssClass,
        /**
         * 表格字典样式
         */
        String listClass,
        /**
         * 是否默认（1是 0否）
         */
        String isDefault,
        /**
         * 状态（1正常 2停用）
         */
        String status,
        /**
         * 备注
         */
        String remark
) implements Serializable {
}
