package com.lemnt.system.application.auth.cmd;

import java.io.Serializable;

/**
 * 修改角色状态命令
 *
 * @author wangxx
 */
public record RoleStatusUpdateCmd(
        /**
         * 角色ID
         */
        Long roleId,
        /**
         * 角色状态（1正常 2停用）
         */
        String status
) implements Serializable {
}
