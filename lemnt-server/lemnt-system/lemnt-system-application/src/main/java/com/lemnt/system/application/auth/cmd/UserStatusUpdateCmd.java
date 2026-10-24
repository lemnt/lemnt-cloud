package com.lemnt.system.application.auth.cmd;

import java.io.Serializable;

/**
 * 修改用户状态命令
 *
 * @author wangxx
 */
public record UserStatusUpdateCmd(
        /**
         * 用户ID
         */
        Long userId,
        /**
         * 账号状态（1正常 2停用）
         */
        String status
) implements Serializable {
}
