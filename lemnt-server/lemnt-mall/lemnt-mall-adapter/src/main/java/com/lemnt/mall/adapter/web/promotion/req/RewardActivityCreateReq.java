package com.lemnt.mall.adapter.web.promotion.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

/** 满减活动新增请求。 */
@Schema(description = "满减活动新增请求")
public record RewardActivityCreateReq(
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
        @Schema(description = "PRICE 满金额或 COUNT 满件数")
        @NotNull
        String conditionType,
        @Schema(description = "ALL 全部、SPU_IDS 指定商品、CATEGORY_IDS 指定分类")
        @NotNull
        String productScope,
        @Schema(description = "适用商品或分类编号集合")
        List<Long> productScopeValues,
        @Schema(description = "按门槛递增的满减规则")
        @NotEmpty @Valid
        List<RewardRuleReq> rules
) implements java.io.Serializable {
}
