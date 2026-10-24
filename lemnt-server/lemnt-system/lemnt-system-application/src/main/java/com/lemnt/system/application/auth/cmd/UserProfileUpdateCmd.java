package com.lemnt.system.application.auth.cmd;

import java.io.Serializable;

/**
 * 修改用户信息命令
 *
 * @author wangxx
 */
public record UserProfileUpdateCmd(
        /**
         * 用户ID
         */
        Long userId,
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
        String sex
) implements Serializable {
}
