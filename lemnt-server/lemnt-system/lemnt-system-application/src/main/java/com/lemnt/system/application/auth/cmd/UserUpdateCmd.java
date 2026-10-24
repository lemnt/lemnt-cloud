package com.lemnt.system.application.auth.cmd;

import java.io.Serializable;
import java.util.List;

/**
 * 修改用户命令
 *
 * @author wangxx
 */
public record UserUpdateCmd(
        /**
         * 用户ID
         */
        Long userId,
        /**
         * 部门ID
         */
        Long deptId,
        /**
         * 用户昵称
         */
        String nickName,
        /**
         * 用户邮箱
         */
        String email,
        /**
         * 手机号码
         */
        String phonenumber,
        /**
         * 用户性别（0男 1女 2未知）
         */
        String sex,
        /**
         * 账号状态（1正常 2停用）
         */
        String status,
        /**
         * 角色ID组
         */
        List<Long> roleIds,
        /**
         * 岗位ID组
         */
        List<Long> postIds
) implements Serializable {
}
