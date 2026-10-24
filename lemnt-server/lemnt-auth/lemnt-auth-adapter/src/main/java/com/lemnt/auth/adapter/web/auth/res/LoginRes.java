package com.lemnt.auth.adapter.web.auth.res;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;

/**
 * 登录响应。
 *
 * @author wangxx
 */
@Schema(name = "LoginRes", description = "用户登录成功后的令牌信息")
public record LoginRes(
        @Schema(description = "JWT 访问令牌", example = "eyJhbGciOiJIUzUxMiJ9...",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String accessToken,
        @Schema(description = "令牌有效期，单位：分钟", example = "30",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Long expiresIn
) implements Serializable {
}
