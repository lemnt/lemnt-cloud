package com.lemnt.system.application.org.cmd;

import java.io.Serializable;

/**
 * 修改部门命令
 *
 * @author wangxx
 */
public record DeptUpdateCmd(
        /**
         * 部门ID
         */
        Long deptId,
        /**
         * 父部门ID
         */
        Long parentId,
        /**
         * 祖级列表
         */
        String ancestors,
        /**
         * 部门名称
         */
        String deptName,
        /**
         * 显示顺序
         */
        Integer orderNum,
        /**
         * 负责人
         */
        String leader,
        /**
         * 联系电话
         */
        String phone,
        /**
         * 邮箱
         */
        String email,
        /**
         * 部门状态（1正常 2停用）
         */
        String status
) implements Serializable {
}
