package com.lemnt.mall.adapter.web.promotion.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/** 满减活动分页响应。 */
@Schema(description = "满减活动分页响应")
public record RewardActivityPageRes(
        @Schema(description = "活动编号")
        Long id,
        @Schema(description = "活动名称")
        String name,
        @Schema(description = "0 开启，1 关闭")
        Integer status,
        @Schema(description = "开始时间，yyyy-MM-dd HH:mm:ss")
        LocalDateTime startTime,
        @Schema(description = "结束时间，yyyy-MM-dd HH:mm:ss")
        LocalDateTime endTime,
        @Schema(description = "备注")
        String remark
) implements java.io.Serializable {
}
