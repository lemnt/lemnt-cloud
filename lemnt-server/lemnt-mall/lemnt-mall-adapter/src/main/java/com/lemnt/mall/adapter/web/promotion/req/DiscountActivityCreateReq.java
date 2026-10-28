package com.lemnt.mall.adapter.web.promotion.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

/** 折扣活动新增请求。 */
@Schema(description = "折扣活动新增请求")
public record DiscountActivityCreateReq(
        @Schema(description = "活动名称")
        @NotBlank @Size(max = 100)
        String name,
        @Schema(description = "开始时间，yyyy-MM-dd HH:mm:ss")
        @NotNull
        LocalDateTime startTime,
        @Schema(description = "结束时间，yyyy-MM-dd HH:mm:ss")
        @NotNull
        LocalDateTime endTime,
        @Schema(description = "备注")
        String remark,
        @Schema(description = "SKU 折扣配置")
        @NotEmpty @Valid
        List<DiscountProductReq> products
) implements java.io.Serializable {
}
