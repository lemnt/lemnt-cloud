package com.lemnt.member.adapter.web.account.req;

@io.swagger.v3.oas.annotations.media.Schema(description = "AdminProfileUpdate请求")
public record AdminProfileUpdateReq(
        @io.swagger.v3.oas.annotations.media.Schema(description = "昵称")
                @jakarta.validation.constraints.NotBlank
                @jakarta.validation.constraints.Size(max = 80)
                String nickname,
        @io.swagger.v3.oas.annotations.media.Schema(description = "邮箱")
                @jakarta.validation.constraints.Size(max = 254)
                String email,
        @io.swagger.v3.oas.annotations.media.Schema(description = "头像HTTPS地址")
                @jakarta.validation.constraints.Size(max = 500)
                String avatar,
        @io.swagger.v3.oas.annotations.media.Schema(description = "性别：0未知，1男，2女") Integer sex,
        @io.swagger.v3.oas.annotations.media.Schema(description = "出生日期")
                java.time.LocalDate birthday,
        @io.swagger.v3.oas.annotations.media.Schema(description = "状态：1启用，0停用") Integer status)
        implements java.io.Serializable {}
