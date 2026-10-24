package com.lemnt.system.application.org.cmd;

import java.io.Serializable;

/**
 * 修改岗位命令
 *
 * @author wangxx
 */
public record PostUpdateCmd(
        /**
         * 岗位ID
         */
        Long postId,
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
