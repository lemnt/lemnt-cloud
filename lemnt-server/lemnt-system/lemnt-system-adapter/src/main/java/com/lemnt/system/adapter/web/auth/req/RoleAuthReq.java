package com.lemnt.system.adapter.web.auth.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * 角色授权用户请求
 *
 * @author wangxx
 */
@Schema(description = "角色授权用户请求")
public record RoleAuthReq(
        @NotNull(message = "角色ID不能为空")
        @Schema(description = "角色ID")
        Long roleId,
        @NotEmpty(message = "用户ID列表不能为空")
        @Schema(description = "用户ID列表")
        List<Long> userIds
) implements Serializable {}
