package com.lemnt.mall.adapter.web.promotion.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

/** 满减活动详情响应。 */
@Schema(description = "满减活动详情响应")
public record RewardActivityDetailRes(
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
        String remark,
        @Schema(description = "PRICE 满金额或 COUNT 满件数")
        String conditionType,
        @Schema(description = "ALL 全部、SPU_IDS 指定商品、CATEGORY_IDS 指定分类")
        String productScope,
        @Schema(description = "适用商品或分类编号集合")
        List<Long> productScopeValues,
        @Schema(description = "按门槛递增的满减规则")
        List<RewardRuleRes> rules
) implements java.io.Serializable {
}
