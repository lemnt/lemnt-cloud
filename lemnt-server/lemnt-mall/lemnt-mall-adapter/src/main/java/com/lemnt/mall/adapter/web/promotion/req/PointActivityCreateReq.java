package com.lemnt.mall.adapter.web.promotion.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/** 积分商城活动新增请求。 */
@Schema(description = "积分商城活动新增请求")
public record PointActivityCreateReq(
        @Schema(description = "商品 SPU 编号")
        @NotNull
        Long spuId,
        @Schema(description = "备注")
        @Size(max = 500)
        String remark,
        @Schema(description = "排序")
        @NotNull
        Integer sort,
        @Schema(description = "积分商品")
        @NotEmpty @Valid
        List<PointProductReq> products
) implements java.io.Serializable {
}
