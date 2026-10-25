package com.lemnt.member.adapter.web.account.req;

@io.swagger.v3.oas.annotations.media.Schema(description = "MemberCreate请求")
public record MemberCreateReq(
        @io.swagger.v3.oas.annotations.media.Schema(description = "昵称")
                @jakarta.validation.constraints.NotBlank
                @jakarta.validation.constraints.Size(max = 80)
                String nickname,
        @io.swagger.v3.oas.annotations.media.Schema(description = "手机号")
                @jakarta.validation.constraints.NotBlank
                @jakarta.validation.constraints.Size(max = 24)
                String mobile,
        @io.swagger.v3.oas.annotations.media.Schema(description = "邮箱")
                @jakarta.validation.constraints.Size(max = 254)
                String email,
        @io.swagger.v3.oas.annotations.media.Schema(description = "头像HTTPS地址")
                @jakarta.validation.constraints.Size(max = 500)
                String avatar)
        implements java.io.Serializable {}
