package com.lemnt.system.adapter.web.auth.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.util.Set;

/**
 * 用户信息响应（前端路由初始化）
 *
 * @author wangxx
 */
@Schema(description = "用户信息响应")
public record UserInfoRes(
        @Schema(description = "用户信息")
        UserRes user,
        @Schema(description = "角色集合")
        Set<String> roles,
        @Schema(description = "权限集合")
        Set<String> permissions
) implements Serializable {}
