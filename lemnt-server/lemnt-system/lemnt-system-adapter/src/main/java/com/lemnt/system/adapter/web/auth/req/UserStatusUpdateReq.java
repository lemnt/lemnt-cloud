package com.lemnt.system.adapter.web.auth.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.io.Serializable;

/**
 * 修改用户状态请求
 *
 * @author wangxx
 */
@Schema(description = "修改用户状态请求")
public record UserStatusUpdateReq(
        @NotNull(message = "用户ID不能为空")
        @Schema(description = "用户ID")
        Long userId,
        @NotBlank(message = "账号状态不能为空")
        @Schema(description = "账号状态（1正常 2停用）")
        @Pattern(regexp = "[12]", message = "状态值只能为1或2")
        String status
) implements Serializable {}
