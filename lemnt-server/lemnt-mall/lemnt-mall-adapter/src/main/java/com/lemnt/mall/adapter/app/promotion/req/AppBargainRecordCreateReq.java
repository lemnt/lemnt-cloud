package com.lemnt.mall.adapter.app.promotion.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;

/** App 发起砍价请求。 */
@Schema(description = "App 发起砍价请求")
public record AppBargainRecordCreateReq(
        @Schema(description = "砍价活动编号")
        @NotNull
        Long activityId
) implements java.io.Serializable {
}
