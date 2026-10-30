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
public record MemberRecordPointRecordsReq(
        @Schema(description = "可选会员ID；未填查询所有会员")
        @Positive
        Long memberId,
        @Schema(description = "当前会员昵称包含匹配")
        @Size(max=80)
        String nickname,
        @Schema(description = "当前业务类型字符串精确匹配")
        @Size(max=40)
        String bizType,
        @Schema(description = "源积分业务枚举编号")
        @Min(1) @Max(23)
        Integer pointBizType,
        @Schema(description = "积分标题包含匹配")
        @Size(max = 255)
        String title,
        @Schema(description = "页码，默认1")
        @Min(1)
        @Max(1000000)
        Integer pageNum,
        @Schema(description = "页大小，默认20，最大100")
        @Min(1)
        @Max(100)
        Integer pageSize
) implements Serializable {}
