package com.lemnt.member.adapter.web.growth.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

/** 后台等级列表独立筛选，状态沿项目1启用0停用。 */
public record MemberLevelListReq(
        @Schema(description = "等级名称包含匹配") @Size(max = 80) String name,
        @Schema(description = "状态：1启用，0停用") @Min(0) @Max(1) Integer status
) implements Serializable {}
