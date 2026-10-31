package com.lemnt.system.adapter.web.auth.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 重置用户密码请求
 *
 * @author wangxx
 */
@Schema(description = "重置用户密码请求")
public record UserPwdResetReq(
        @NotNull(message = "用户ID不能为空")
        @Schema(description = "用户ID")
        Long userId,
        @NotBlank(message = "密码不能为空")
        @Schema(description = "密码")
        String password
) implements Serializable {}
