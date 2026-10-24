package com.lemnt.system.application.auth.cmd;

import java.io.Serializable;
import java.util.List;

/**
 * 角色授权用户
 *
 * @author wangxx
 */
public record RoleAuthCmd(
        /**
         * 角色ID
         */
        Long roleId,
        /**
         * 用户ID列表
         */
        List<Long> userIds
) implements Serializable {
}
