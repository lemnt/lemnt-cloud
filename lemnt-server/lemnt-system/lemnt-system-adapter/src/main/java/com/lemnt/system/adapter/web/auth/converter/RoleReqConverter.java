package com.lemnt.system.adapter.web.auth.converter;

import com.lemnt.system.adapter.web.auth.req.*;
import com.lemnt.system.adapter.web.auth.req.SystemRoleUserCancelAuthUserAllReq;
import com.lemnt.system.adapter.web.auth.req.SystemRoleUserCancelAuthUserReq;
import com.lemnt.system.application.auth.cmd.RoleAddCmd;
import com.lemnt.system.application.auth.cmd.RoleAuthCancelCmd;
import com.lemnt.system.application.auth.cmd.RoleAuthCmd;
import com.lemnt.system.application.auth.cmd.RoleDataScopeUpdateCmd;
import com.lemnt.system.application.auth.cmd.RoleStatusUpdateCmd;
import com.lemnt.system.application.auth.cmd.RoleUpdateCmd;
import com.lemnt.system.application.auth.qry.RolePageQry;

/**
 * 角色请求对象转换器
 *
 * @author wangxx
 */
public class RoleReqConverter {
    public static RoleAddCmd toCmd(RoleAddReq req) {
        return new RoleAddCmd(
                req.roleName(),
                req.roleKey(),
                req.roleSort(),
                req.dataScope(),
                req.status(),
                req.menuIds()
        );
    }

    public static RoleUpdateCmd toCmd(RoleUpdateReq req) {
        return new RoleUpdateCmd(
                req.roleId(),
                req.roleName(),
                req.roleKey(),
                req.roleSort(),
                req.dataScope(),
                req.status(),
                req.menuIds()
        );
    }

    public static RoleDataScopeUpdateCmd toCmd(RoleDataScopeUpdateReq req) {
        return new RoleDataScopeUpdateCmd(
                req.roleId(),
                req.dataScope(),
                req.deptIds()
        );
    }

    public static RoleStatusUpdateCmd toCmd(RoleStatusUpdateReq req) {
        return new RoleStatusUpdateCmd(
                req.roleId(),
                req.status()
        );
    }

    public static RoleAuthCancelCmd toAuthCancelCmd(RoleAuthCancelReq req) {
        return new RoleAuthCancelCmd(
                req.roleId(),
                req.userId(),
                req.userIds()
        );
    }

    public static RoleAuthCmd toAuthCmd(RoleAuthReq req) {
        return new RoleAuthCmd(
                req.roleId(),
                req.userIds()
        );
    }

    public static RolePageQry toQry(RolePageReq item) {
        if (item == null) {
            return null;
        }
        return new RolePageQry(
                item.roleName(),
                item.roleKey(),
                item.status(),
                item.pageNum(),
                item.pageSize(),
                item.beginTime(),
                item.endTime()
        );
    }

    public static RoleAuthCancelCmd toAuthCancelCmd(SystemRoleUserCancelAuthUserReq req) {
        return new RoleAuthCancelCmd(
                req.roleId(),
                req.userId(),
                req.userIds()
        );
    }

    public static RoleAuthCancelCmd toAuthCancelCmd(SystemRoleUserCancelAuthUserAllReq req) {
        return new RoleAuthCancelCmd(
                req.roleId(),
                req.userId(),
                req.userIds()
        );
    }
}
