package com.lemnt.system.application.org.cmd;

import java.io.Serializable;

/**
 * 新增岗位命令
 *
 * @author wangxx
 */
public record PostAddCmd(
        /**
         * 岗位名称
         */
        String postName,
        /**
         * 岗位编码
         */
        String postCode,
        /**
         * 岗位排序
         */
        Integer postSort,
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
