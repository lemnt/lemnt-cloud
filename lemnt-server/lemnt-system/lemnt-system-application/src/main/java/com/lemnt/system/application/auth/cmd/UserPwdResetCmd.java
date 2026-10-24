package com.lemnt.system.application.auth.cmd;

import java.io.Serializable;

/**
 * 重置用户密码命令
 *
 * @author wangxx
 */
public record UserPwdResetCmd(
        /**
         * 用户ID
         */
        Long userId,
        /**
         * 密码
         */
        String password
) implements Serializable {
}
