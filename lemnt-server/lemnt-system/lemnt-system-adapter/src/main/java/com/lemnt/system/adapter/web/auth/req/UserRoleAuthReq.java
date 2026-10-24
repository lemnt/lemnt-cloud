package com.lemnt.system.adapter.web.auth.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.util.List;

/**
 * 用户授权角色请求
 *
 * @author wangxx
 */
@Schema(description = "用户授权角色请求")
public record UserRoleAuthReq(
        @NotNull(message = "用户ID不能为空")
        @Schema(description = "用户ID")
        Long userId,
        @Schema(description = "角色ID组")
        List<Long> roleIds
) implements Serializable {}
