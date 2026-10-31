package com.lemnt.mall.adapter.app.promotion.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;

/** App 帮砍请求。 */
@Schema(description = "App 帮砍请求")
public record AppBargainHelpCreateReq(
        @Schema(description = "砍价记录编号")
        @NotNull
        Long recordId
) implements java.io.Serializable {
}
