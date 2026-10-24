package com.lemnt.system.application.auth.cmd;

import java.io.Serializable;
import java.util.List;

/**
 * 新增用户命令
 *
 * @author wangxx
 */
public record UserAddCmd(
        /**
         * 部门ID
         */
        Long deptId,
        /**
         * 用户账号
         */
        String userName,
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
         * 密码
         */
        String password,
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
