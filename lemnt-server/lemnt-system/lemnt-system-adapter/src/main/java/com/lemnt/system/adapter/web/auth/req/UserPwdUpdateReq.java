package com.lemnt.system.adapter.web.auth.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 修改个人密码请求
 *
 * @author wangxx
 */
@Schema(description = "修改个人密码请求")
public record UserPwdUpdateReq(
        @NotNull(message = "用户ID不能为空")
        @Schema(description = "用户ID")
        Long userId,
        @NotBlank(message = "旧密码不能为空")
        @Schema(description = "旧密码")
        String oldPassword,
        @NotBlank(message = "新密码不能为空")
        @Schema(description = "新密码")
        String newPassword
) implements Serializable {}
