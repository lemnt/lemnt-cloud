package com.lemnt.system.application.auth.cmd;

import java.io.Serializable;
import java.util.List;

/**
 * 用户角色授权命令
 *
 * @author wangxx
 */
public record UserRoleAuthCmd(
        /**
         * 用户ID
         */
        Long userId,
        /**
         * 角色ID组
         */
        List<Long> roleIds
) implements Serializable {
}
