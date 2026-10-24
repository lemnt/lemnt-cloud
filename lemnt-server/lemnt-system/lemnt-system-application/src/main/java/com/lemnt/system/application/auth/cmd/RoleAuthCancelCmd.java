package com.lemnt.system.application.auth.cmd;

import java.io.Serializable;
import java.util.List;

/**
 * 角色取消用户授权
 *
 * @author wangxx
 */
public record RoleAuthCancelCmd(
        /**
         * 角色ID
         */
        Long roleId,
        /**
         * 用户ID
         */
        Long userId,
        /**
         * 用户ID列表
         */
        List<Long> userIds
) implements Serializable {
}
