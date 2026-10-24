package com.lemnt.system.application.config.cmd;

import java.io.Serializable;

/**
 * 新增字典类型命令
 *
 * @author wangxx
 */
public record DictTypeAddCmd(
        /**
         * 字典名称
         */
        String dictName,
        /**
         * 字典类型
         */
        String dictType,
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
