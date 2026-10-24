package com.lemnt.system.application.auth.qry;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 角色分页查询条件
 *
 * @author wangxx
 */
public record RolePageQry(
        /**
         * 角色名称
         */
        String roleName,
        /**
         * 角色权限标识
         */
        String roleKey,
        /**
         * 角色状态（1正常 2停用）
         */
        String status,
        int pageNum,
        int pageSize,
        LocalDateTime beginTime,
        LocalDateTime endTime
) implements Serializable {
}
