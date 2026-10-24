package com.lemnt.system.application.auth.cmd;

import java.io.Serializable;

/**
 * 修改用户密码命令
 *
 * @author wangxx
 */
public record UserPwdUpdateCmd(
        /**
         * 用户ID
         */
        Long userId,
        /**
         * 密码
         */
        String oldPassword,
        /**
         * 新密码
         */
        String newPassword
) implements Serializable {
}
