package com.lemnt.member.adapter.web.growth.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

/**
 * 后台会员分页查询。
 */
@Schema(description = "后台会员分页查询")
public record MemberCatalogQueryTagsPageReq(
        @Schema(description = "可选会员ID；未填查询所有会员")
        @Positive
        Long memberId,
        @Schema(description = "名称包含匹配")
        @Size(max = 100)
        String name,
        @Schema(description = "状态：0禁用，1启用")
        @Min(0)
        @Max(1)
        Integer status,
        @Schema(description = "页码，默认1")
        @Min(1)
        @Max(1000000)
        Integer pageNum,
        @Schema(description = "页大小，默认20，最大100")
        @Min(1)
        @Max(100)
        Integer pageSize
) implements Serializable {}
