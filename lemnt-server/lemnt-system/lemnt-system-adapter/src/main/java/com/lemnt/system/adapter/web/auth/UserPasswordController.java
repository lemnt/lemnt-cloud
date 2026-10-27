package com.lemnt.system.adapter.web.auth;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.log.annotation.Log;
import com.lemnt.common.log.enums.BusinessType;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.system.adapter.web.auth.converter.UserReqConverter;
import com.lemnt.system.adapter.web.auth.req.*;
import com.lemnt.system.adapter.web.auth.res.*;
import com.lemnt.system.application.auth.executor.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

/** 用户密码接口。 */
@Tag(name = "用户密码")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserPasswordController {
    private final UserResetPwdCmdExe userResetPwdCmdExe;

    @Operation(summary = "重置密码")
    @RequiresPermissions("system:user:resetPwd")
    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    @PutMapping("/reset-password")
    public R<Boolean> resetPwd(@RequestBody UserPwdResetReq req) {
        return R.ok(userResetPwdCmdExe.execute(UserReqConverter.toCmd(req)));
    }
}
