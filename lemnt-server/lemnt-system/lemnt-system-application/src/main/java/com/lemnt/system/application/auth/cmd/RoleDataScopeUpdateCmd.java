package com.lemnt.system.application.auth.cmd;

import java.io.Serializable;
import java.util.List;

/**
 * 修改角色数据范围命令
 *
 * @author wangxx
 */
public record RoleDataScopeUpdateCmd(
        /**
         * 角色ID
         */
        Long roleId,
        /**
         * 数据范围（1：全部数据权限 2：自定数据权限 3：本部门数据权限 4：本部门及以下数据权限）
         */
        String dataScope,
        /**
         * 部门组
         */
        List<Long> deptIds
) implements Serializable {
}
