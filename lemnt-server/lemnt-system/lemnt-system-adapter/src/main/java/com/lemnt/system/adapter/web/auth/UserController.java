package com.lemnt.system.adapter.web.auth;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.domain.model.PageResult;
import com.lemnt.common.log.annotation.Log;
import com.lemnt.common.log.enums.BusinessType;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.system.adapter.web.auth.converter.UserReqConverter;
import com.lemnt.system.adapter.web.auth.converter.UserResConverter;
import com.lemnt.system.adapter.web.auth.req.*;
import com.lemnt.system.adapter.web.auth.req.SystemUserListReq;
import com.lemnt.system.adapter.web.auth.res.*;
import com.lemnt.system.adapter.web.auth.res.SystemUserListRes;
import com.lemnt.system.application.auth.dto.UserDTO;
import com.lemnt.system.application.auth.executor.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import java.util.List;

/** 用户管理接口。 */
@Tag(name = "用户管理")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserPageQueryExe userPageQueryExe;
    private final UserDetailQueryExe userDetailQueryExe;
    private final UserAddCmdExe userAddCmdExe;
    private final UserUpdateCmdExe userUpdateCmdExe;
    private final UserDeleteCmdExe userDeleteCmdExe;
    private final UserUpdateStatusCmdExe userUpdateStatusCmdExe;

    @Operation(summary = "获取用户列表")
    @RequiresPermissions("system:user:list")
    @GetMapping
    public R<SystemUserListRes> list(@Validated SystemUserListReq req) {
        PageResult<UserDTO> result = userPageQueryExe.execute(UserReqConverter.toQry(req));
        return R.page(UserResConverter.toResListToSystemUserListRes(result.getRecords()), result.getTotal());
    }

    @Operation(summary = "根据用户编号获取详细信息")
    @RequiresPermissions("system:user:query")
    @GetMapping(value = {
            "/options", "/{userId}"
            })
    public R<UserDetailRes> getDetail(@PathVariable(value = "userId", required = false) Long userId) {
        return R.ok(UserResConverter.toDetailRes(userDetailQueryExe.execute(userId)));
    }

    @Operation(summary = "新增用户")
    @RequiresPermissions("system:user:add")
    @Log(title = "用户管理", businessType = BusinessType.INSERT)
    @PostMapping
    public R<Boolean> add(@Validated @RequestBody UserAddReq req) {
        return R.ok(userAddCmdExe.execute(UserReqConverter.toCmd(req)));
    }

    @Operation(summary = "修改用户")
    @RequiresPermissions("system:user:edit")
    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public R<Boolean> edit(@Validated @RequestBody UserUpdateReq req) {
        return R.ok(userUpdateCmdExe.execute(UserReqConverter.toCmd(req)));
    }

    @Operation(summary = "删除用户")
    @RequiresPermissions("system:user:remove")
    @Log(title = "用户管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{userIds}")
    public R<Boolean> remove(@PathVariable List<Long> userIds) {
        return R.ok(userDeleteCmdExe.execute(userIds));
    }

    @Operation(summary = "状态修改")
    @RequiresPermissions("system:user:edit")
    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    @PutMapping("/change-status")
    public R<Boolean> changeStatus(@Validated @RequestBody UserStatusUpdateReq req) {
        return R.ok(userUpdateStatusCmdExe.execute(UserReqConverter.toCmd(req)));
    }
}
