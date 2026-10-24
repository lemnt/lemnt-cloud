package com.lemnt.system.application.auth.cmd;

import java.io.Serializable;
import java.util.List;

/**
 * 修改角色命令
 *
 * @author wangxx
 */
public record RoleUpdateCmd(
        /**
         * 角色ID
         */
        Long roleId,
        /**
         * 角色名称
         */
        String roleName,
        /**
         * 角色权限标识
         */
        String roleKey,
        /**
         * 角色排序
         */
        Integer roleSort,
        /**
         * 数据范围（1：全部数据权限 2：自定数据权限 3：本部门数据权限 4：本部门及以下数据权限）
         */
        String dataScope,
        /**
         * 角色状态（1正常 2停用）
         */
        String status,
        /**
         * 菜单组
         */
        List<Long> menuIds
) implements Serializable {
}
