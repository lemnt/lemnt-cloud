package com.lemnt.system.adapter.web.auth.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 修改个人信息请求
 *
 * @author wangxx
 */
@Schema(description = "修改个人信息请求")
public record UserProfileUpdateReq(
        @NotNull(message = "用户ID不能为空")
        @Schema(description = "用户ID")
        Long userId,
        @Schema(description = "用户昵称")
        String nickName,
        @Schema(description = "用户邮箱")
        String email,
        @Schema(description = "手机号码")
        String phonenumber,
        @Schema(description = "用户性别（0男 1女 2未知）")
        String sex
) implements Serializable {}
