package com.lemnt.system.adapter.web.auth.converter;

import com.lemnt.system.adapter.web.auth.req.*;
import com.lemnt.system.adapter.web.auth.req.SystemRoleUserAllocatedListReq;
import com.lemnt.system.adapter.web.auth.req.SystemRoleUserUnallocatedListReq;
import com.lemnt.system.adapter.web.auth.req.SystemUserListReq;
import com.lemnt.system.application.auth.cmd.UserAddCmd;
import com.lemnt.system.application.auth.cmd.UserProfileUpdateCmd;
import com.lemnt.system.application.auth.cmd.UserPwdResetCmd;
import com.lemnt.system.application.auth.cmd.UserPwdUpdateCmd;
import com.lemnt.system.application.auth.cmd.UserRoleAuthCmd;
import com.lemnt.system.application.auth.cmd.UserStatusUpdateCmd;
import com.lemnt.system.application.auth.cmd.UserUpdateCmd;
import com.lemnt.system.application.auth.qry.UserPageQry;

/**
 * 用户请求对象转换器
 *
 * @author wangxx
 */
public class UserReqConverter {
    public static UserAddCmd toCmd(UserAddReq req) {
        return new UserAddCmd(
                req.deptId(),
                req.userName(),
                req.nickName(),
                req.email(),
                req.phonenumber(),
                req.sex(),
                req.password(),
                req.status(),
                req.roleIds(),
                req.postIds()
        );
    }

    public static UserUpdateCmd toCmd(UserUpdateReq req) {
        return new UserUpdateCmd(
                req.userId(),
                req.deptId(),
                req.nickName(),
                req.email(),
                req.phonenumber(),
                req.sex(),
                req.status(),
                req.roleIds(),
                req.postIds()
        );
    }

    public static UserPwdResetCmd toCmd(UserPwdResetReq req) {
        return new UserPwdResetCmd(
                req.userId(),
                req.password()
        );
    }

    public static UserStatusUpdateCmd toCmd(UserStatusUpdateReq req) {
        return new UserStatusUpdateCmd(
                req.userId(),
                req.status()
        );
    }

    public static UserProfileUpdateCmd toCmd(UserProfileUpdateReq req) {
        return new UserProfileUpdateCmd(
                req.userId(),
                req.nickName(),
                req.email(),
                req.phonenumber(),
                req.sex()
        );
    }

    public static UserPwdUpdateCmd toCmd(UserPwdUpdateReq req) {
        return new UserPwdUpdateCmd(
                req.userId(),
                req.oldPassword(),
                req.newPassword()
        );
    }

    public static UserRoleAuthCmd toCmd(UserRoleAuthReq req) {
        return new UserRoleAuthCmd(
                req.userId(),
                req.roleIds()
        );
    }

    public static UserPageQry toQry(UserPageReq item) {
        if (item == null) {
            return null;
        }
        return new UserPageQry(
                item.userName(),
                item.phonenumber(),
                item.status(),
                item.deptId(),
                item.roleId(),
                item.pageNum(),
                item.pageSize(),
                item.beginTime(),
                item.endTime()
        );
    }

    public static UserPageQry toQry(SystemUserListReq item) {
        if (item == null) {
            return null;
        }
        return new UserPageQry(
                item.userName(),
                item.phonenumber(),
                item.status(),
                item.deptId(),
                item.roleId(),
                item.pageNum(),
                item.pageSize(),
                item.beginTime(),
                item.endTime()
        );
    }

    public static UserPageQry toQry(SystemRoleUserAllocatedListReq item) {
        if (item == null) {
            return null;
        }
        return new UserPageQry(
                item.userName(),
                item.phonenumber(),
                item.status(),
                item.deptId(),
                item.roleId(),
                item.pageNum(),
                item.pageSize(),
                item.beginTime(),
                item.endTime()
        );
    }

    public static UserPageQry toQry(SystemRoleUserUnallocatedListReq item) {
        if (item == null) {
            return null;
        }
        return new UserPageQry(
                item.userName(),
                item.phonenumber(),
                item.status(),
                item.deptId(),
                item.roleId(),
                item.pageNum(),
                item.pageSize(),
                item.beginTime(),
                item.endTime()
        );
    }
}
