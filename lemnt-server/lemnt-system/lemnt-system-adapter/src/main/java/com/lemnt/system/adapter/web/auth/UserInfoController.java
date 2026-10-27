package com.lemnt.system.adapter.web.auth;

import com.lemnt.common.core.domain.R;
import com.lemnt.system.adapter.web.auth.converter.UserResConverter;
import com.lemnt.system.adapter.web.auth.req.*;
import com.lemnt.system.adapter.web.auth.res.*;
import com.lemnt.system.application.auth.converter.UserConverter;
import com.lemnt.system.application.auth.executor.*;
import com.lemnt.system.domain.auth.model.User;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.Set;

/** 当前用户信息接口。 */
@Tag(name = "当前用户信息")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserInfoController {
    private final UserInfoQueryExe userInfoQueryExe;

    @Operation(summary = "获取用户信息（前端路由初始化）")
    @SuppressWarnings("unchecked")
    @GetMapping("/current-info")
    public R<UserInfoRes> getInfo() {
        Map<String, Object> result = userInfoQueryExe.execute();
        UserInfoRes res = new UserInfoRes(
                UserResConverter.toRes(UserConverter.toDTO((User) result.get("user"))),
                (Set<String>) result.get("roles"),
                (Set<String>) result.get("permissions"));
        return R.ok(res);
    }
}
