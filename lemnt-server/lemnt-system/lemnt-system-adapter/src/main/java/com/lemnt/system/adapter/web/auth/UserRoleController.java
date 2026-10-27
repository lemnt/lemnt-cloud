package com.lemnt.system.adapter.web.auth;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.log.annotation.Log;
import com.lemnt.common.log.enums.BusinessType;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.system.adapter.web.auth.converter.RoleResConverter;
import com.lemnt.system.adapter.web.auth.converter.UserReqConverter;
import com.lemnt.system.adapter.web.auth.converter.UserResConverter;
import com.lemnt.system.adapter.web.auth.req.*;
import com.lemnt.system.adapter.web.auth.res.*;
import com.lemnt.system.application.auth.dto.RoleDTO;
import com.lemnt.system.application.auth.dto.UserDTO;
import com.lemnt.system.application.auth.executor.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

/** 用户角色授权接口。 */
@Tag(name = "用户角色授权")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserRoleController {
    private final UserRoleAuthQueryExe userRoleAuthQueryExe;
    private final UserRoleAuthCmdExe userRoleAuthCmdExe;

    @Operation(summary = "根据用户编号获取授权角色")
    @SuppressWarnings("unchecked")
    @RequiresPermissions("system:user:query")
    @GetMapping("/auth-role/{userId}")
    public R<UserAuthRoleRes> authRole(@PathVariable Long userId) {
        Map<String, Object> result = userRoleAuthQueryExe.execute(userId);
        UserAuthRoleRes res = new UserAuthRoleRes(
                UserResConverter.toRes((UserDTO) result.get("user")),
                RoleResConverter.toResList((List<RoleDTO>) result.get("roles")));
        return R.ok(res);
    }

    @Operation(summary = "用户授权角色")
    @RequiresPermissions("system:user:edit")
    @Log(title = "用户管理", businessType = BusinessType.GRANT)
    @PutMapping("/auth-role")
    public R<Boolean> insertAuthRole(@RequestBody UserRoleAuthReq req) {
        return R.ok(userRoleAuthCmdExe.execute(UserReqConverter.toCmd(req)));
    }
}
