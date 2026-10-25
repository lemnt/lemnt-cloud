package com.lemnt.member.adapter.web.account.req;

@io.swagger.v3.oas.annotations.media.Schema(description = "LevelAssign请求")
public record LevelAssignReq(
        @io.swagger.v3.oas.annotations.media.Schema(description = "等级编号，可空") Long levelId,
        @io.swagger.v3.oas.annotations.media.Schema(description = "幂等业务编号，重试不可更改内容")
                @jakarta.validation.constraints.NotBlank
                @jakarta.validation.constraints.Size(max = 64)
                String requestNo,
        @io.swagger.v3.oas.annotations.media.Schema(description = "原因")
                @jakarta.validation.constraints.NotBlank
                @jakarta.validation.constraints.Size(max = 500)
                String reason)
        implements java.io.Serializable {}
