package com.lemnt.member.adapter.web.user.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

/**
 * 账户分页请求。
 */
@Schema(description = "AccountPage请求")
public record MemberUserExperienceRecordsReq(
        @Schema(description = "手机号")
        @Size(max = 24)
        String mobile,
        @Schema(description = "页码，默认1")
        @Min(1)
        @Max(1000000)
        Integer pageNum,
        @Schema(description = "每页条数，默认20，最大100")
        @Min(1)
        @Max(100)
        Integer pageSize
) implements Serializable {}
