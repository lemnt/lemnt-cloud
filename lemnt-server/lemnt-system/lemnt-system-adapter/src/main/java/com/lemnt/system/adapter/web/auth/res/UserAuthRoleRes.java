package com.lemnt.system.adapter.web.auth.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.util.List;

/**
 * 用户授权角色详情响应
 *
 * @author wangxx
 */
@Schema(description = "用户授权角色详情响应")
public record UserAuthRoleRes(
        @Schema(description = "用户信息")
        UserRes user,
        @Schema(description = "可授权角色列表")
        List<RoleRes> roles
) implements Serializable {}
