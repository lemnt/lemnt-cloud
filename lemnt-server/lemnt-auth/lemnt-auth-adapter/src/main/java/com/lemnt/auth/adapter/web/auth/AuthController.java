package com.lemnt.auth.adapter.web.auth;

import com.lemnt.auth.adapter.web.auth.converter.LoginConverter;
import com.lemnt.auth.adapter.web.auth.converter.UnLockConverter;
import com.lemnt.auth.adapter.web.auth.req.LoginReq;
import com.lemnt.auth.adapter.web.auth.req.UnLockReq;
import com.lemnt.auth.adapter.web.auth.res.LoginRes;
import com.lemnt.auth.application.auth.executor.*;
import com.lemnt.common.core.domain.R;
import com.lemnt.common.core.utils.StringUtils;
import com.lemnt.common.security.utils.SecurityUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 认证管理 Controller（CQRS 新架构）
 *
 * @author wangxx
 */
@Tag(name = "认证管理")
@RestController
@RequestMapping("/sessions")
@RequiredArgsConstructor
public class AuthController {
    private final LoginCmdExe loginCmdExe;
    private final LogoutCmdExe logoutCmdExe;
    private final RefreshCmdExe refreshCmdExe;
    private final UnLockCmdExe unlockCmdExe;

    @Operation(summary = "登录")
    @PostMapping("login")
    public R<LoginRes> login(@RequestBody LoginReq req) {
        var result = loginCmdExe.execute(LoginConverter.toCmd(req));
        return R.ok(LoginConverter.toRes(result));
    }

    @Operation(summary = "退出登录")
    @DeleteMapping("logout")
    public R<Boolean> logout(HttpServletRequest request) {
        String token = SecurityUtils.getToken(request);
        if (StringUtils.isEmpty(token)) {
            return R.ok(Boolean.FALSE);
        }
        return R.ok(logoutCmdExe.execute(token));
    }

    @Operation(summary = "刷新令牌有效期")
    @PostMapping("refresh")
    public R<Boolean> refresh(HttpServletRequest request) {
        String token = SecurityUtils.getToken(request);
        if (StringUtils.isEmpty(token)) {
            return R.ok(Boolean.FALSE);
        }
        return R.ok(refreshCmdExe.execute(token));
    }

    @Operation(summary = "解锁屏幕")
    @PostMapping("/screen-unlocks")
    public R<Boolean> unlockScreen(@RequestBody UnLockReq req) {
        return R.ok(unlockCmdExe.execute(UnLockConverter.toCmd(req)));
    }
}
