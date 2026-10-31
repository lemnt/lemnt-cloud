package com.lemnt.auth.adapter.web.auth.req;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;

/**
 * 系统解锁请求。
 *
 * @author wangxx
 */
@Schema(name = "UnLockReq", description = "锁定屏幕解锁请求")
public record UnLockReq(
        @Schema(description = "当前登录用户密码", example = "admin123", format = "password",
        requiredMode = Schema.RequiredMode.REQUIRED)
        String password
) implements Serializable {
}
