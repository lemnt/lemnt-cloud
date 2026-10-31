package com.lemnt.system.adapter.web.auth;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.log.annotation.Log;
import com.lemnt.common.log.enums.BusinessType;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.system.adapter.web.auth.req.*;
import com.lemnt.system.adapter.web.auth.res.*;
import com.lemnt.system.application.auth.executor.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

/** 用户登录锁定接口。 */
@Tag(name = "用户登录锁定")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserLockController {
    private final UserUnlockCmdExe userUnlockCmdExe;

    @Operation(summary = "解锁用户（清除登录失败次数限制）")
    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    @RequiresPermissions("system:user:edit")
    @PutMapping("/unlock/{userName}")
    public R<Boolean> unlock(@PathVariable String userName) {
        return R.ok(userUnlockCmdExe.execute(userName));
    }
}
