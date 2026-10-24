package com.lemnt.system.adapter.web.auth.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;

@Schema(description = "个人资料响应")
public record UserProfileRes(
        @Schema(description = "用户信息")
        UserRes user,
        @Schema(description = "角色组")
        String roleGroup,
        @Schema(description = "岗位组")
        String postGroup
) implements Serializable {
}
