package com.lemnt.member.adapter.web.account.req;

@io.swagger.v3.oas.annotations.media.Schema(description = "AccountPage请求")
public record AccountPageReq(
        @io.swagger.v3.oas.annotations.media.Schema(description = "手机号")
                @jakarta.validation.constraints.Size(max = 24)
                String mobile,
        @io.swagger.v3.oas.annotations.media.Schema(description = "页码，默认1")
                @jakarta.validation.constraints.Min(1)
                @jakarta.validation.constraints.Max(1000000)
                Integer pageNum,
        @io.swagger.v3.oas.annotations.media.Schema(description = "每页条数，默认20，最大100")
                @jakarta.validation.constraints.Min(1)
                @jakarta.validation.constraints.Max(100)
                Integer pageSize)
        implements java.io.Serializable {}
