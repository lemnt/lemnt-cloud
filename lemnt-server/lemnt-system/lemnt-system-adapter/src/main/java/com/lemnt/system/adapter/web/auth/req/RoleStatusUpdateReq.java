package com.lemnt.system.adapter.web.auth.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

/**
 * 修改角色状态请求
 *
 * @author wangxx
 */
@Schema(description = "修改角色状态请求")
public record RoleStatusUpdateReq(
        @NotNull(message = "角色ID不能为空")
        @Schema(description = "角色ID")
        Long roleId,
        @NotBlank(message = "角色状态不能为空")
        @Schema(description = "角色状态（1正常 2停用）")
        @Pattern(regexp = "[12]", message = "状态值只能为1或2")
        String status
) implements Serializable {}
