package com.lemnt.auth.adapter.web.auth.req;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;

@Schema(name = "LoginReq", description = "用户登录请求")
public record LoginReq(
        @Schema(description = "登录用户名", example = "admin", requiredMode = Schema.RequiredMode.REQUIRED)
        String username,
        @Schema(description = "登录密码", example = "admin123", format = "password",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String password
) implements Serializable {
}
