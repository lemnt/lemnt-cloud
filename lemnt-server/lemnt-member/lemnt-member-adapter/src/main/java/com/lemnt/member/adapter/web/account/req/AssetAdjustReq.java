package com.lemnt.member.adapter.web.account.req;

@io.swagger.v3.oas.annotations.media.Schema(description = "AssetAdjust请求")
public record AssetAdjustReq(
        @io.swagger.v3.oas.annotations.media.Schema(description = "本次有符号变更量")
                @jakarta.validation.constraints.NotNull
                Long delta,
        @io.swagger.v3.oas.annotations.media.Schema(description = "幂等业务编号，重试不可更改内容")
                @jakarta.validation.constraints.NotBlank
                @jakarta.validation.constraints.Size(max = 64)
                String requestNo,
        @io.swagger.v3.oas.annotations.media.Schema(description = "原因")
                @jakarta.validation.constraints.NotBlank
                @jakarta.validation.constraints.Size(max = 500)
                String reason)
        implements java.io.Serializable {}
