package com.lemnt.system.adapter.web.auth.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;

@Schema(description = "头像上传响应")
public record UserAvatarRes(
        @Schema(description = "头像访问地址")
        String imgUrl
) implements Serializable {
}
