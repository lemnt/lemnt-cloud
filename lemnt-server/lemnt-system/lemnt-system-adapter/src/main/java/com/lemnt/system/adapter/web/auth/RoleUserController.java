package com.lemnt.system.adapter.web.auth;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.domain.model.PageResult;
import com.lemnt.common.log.annotation.Log;
import com.lemnt.common.log.enums.BusinessType;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.system.adapter.web.auth.converter.*;
import com.lemnt.system.adapter.web.auth.req.*;
import com.lemnt.system.adapter.web.auth.req.SystemRoleUserAllocatedListReq;
import com.lemnt.system.adapter.web.auth.req.SystemRoleUserCancelAuthUserAllReq;
import com.lemnt.system.adapter.web.auth.req.SystemRoleUserCancelAuthUserReq;
import com.lemnt.system.adapter.web.auth.req.SystemRoleUserUnallocatedListReq;
import com.lemnt.system.adapter.web.auth.res.*;
import com.lemnt.system.adapter.web.auth.res.SystemRoleUserAllocatedListRes;
import com.lemnt.system.adapter.web.auth.res.SystemRoleUserUnallocatedListRes;
import com.lemnt.system.application.auth.dto.UserDTO;
import com.lemnt.system.application.auth.executor.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

/** 角色用户授权接口。 */
@Tag(name = "角色用户授权")
@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
public class RoleUserController {
    private final RoleAuthBatchCmdExe roleAuthBatchCmdExe;
    private final RoleAuthCancelCmdExe roleAuthCancelCmdExe;
    private final RoleAuthCancelBatchCmdExe roleAuthCancelBatchCmdExe;
    private final UserAllocatedPageQueryExe userAllocatedPageQueryExe;
    private final UserUnAllocatedPageQueryExe userUnAllocatedPageQueryExe;

    @Operation(summary = "查询已分配用户角色列表")
    @RequiresPermissions("system:role:list")
    @GetMapping("/auth-user/allocated-list")
    public R<SystemRoleUserAllocatedListRes> allocatedList(@Validated SystemRoleUserAllocatedListReq req) {
        PageResult<UserDTO> result = userAllocatedPageQueryExe.execute(UserReqConverter.toQry(req));
        return R.page(UserResConverter.toResListToSystemRoleUserAllocatedListRes(result.getRecords()),
                result.getTotal());
    }

    @Operation(summary = "查询未分配用户角色列表")
    @RequiresPermissions("system:role:list")
    @GetMapping("/auth-user/unallocated-list")
    public R<SystemRoleUserUnallocatedListRes> unallocatedList(@Validated SystemRoleUserUnallocatedListReq req) {
        PageResult<UserDTO> result = userUnAllocatedPageQueryExe.execute(UserReqConverter.toQry(req));
        return R.page(UserResConverter.toResListToSystemRoleUserUnallocatedListRes(result.getRecords()),
                result.getTotal());
    }

    @Operation(summary = "取消授权用户")
    @RequiresPermissions("system:role:edit")
    @Log(title = "角色管理", businessType = BusinessType.GRANT)
    @PutMapping("/auth-user/cancel")
    public R<Boolean> cancelAuthUser(@RequestBody SystemRoleUserCancelAuthUserReq req) {
        return R.ok(roleAuthCancelCmdExe.execute(RoleReqConverter.toAuthCancelCmd(req)));
    }

    @Operation(summary = "批量取消授权用户")
    @RequiresPermissions("system:role:edit")
    @Log(title = "角色管理", businessType = BusinessType.GRANT)
    @PutMapping("/auth-user/cancel-all")
    public R<Boolean> cancelAuthUserAll(@RequestBody SystemRoleUserCancelAuthUserAllReq req) {
        return R.ok(roleAuthCancelBatchCmdExe.execute(RoleReqConverter.toAuthCancelCmd(req)));
    }

    @Operation(summary = "批量选择用户授权")
    @RequiresPermissions("system:role:edit")
    @Log(title = "角色管理", businessType = BusinessType.GRANT)
    @PutMapping("/auth-user/select-all")
    public R<Boolean> selectAuthUserAll(@RequestBody RoleAuthReq req) {
        return R.ok(roleAuthBatchCmdExe.execute(RoleReqConverter.toAuthCmd(req)));
    }
}
