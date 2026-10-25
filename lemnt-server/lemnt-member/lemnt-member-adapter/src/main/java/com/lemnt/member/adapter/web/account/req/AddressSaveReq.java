package com.lemnt.member.adapter.web.account.req;

@io.swagger.v3.oas.annotations.media.Schema(description = "AddressSave请求")
public record AddressSaveReq(
        @io.swagger.v3.oas.annotations.media.Schema(description = "名称或收件人")
                @jakarta.validation.constraints.NotBlank
                @jakarta.validation.constraints.Size(max = 80)
                String name,
        @io.swagger.v3.oas.annotations.media.Schema(description = "手机号")
                @jakarta.validation.constraints.NotBlank
                @jakarta.validation.constraints.Size(max = 24)
                String mobile,
        @io.swagger.v3.oas.annotations.media.Schema(description = "地区编号")
                @jakarta.validation.constraints.NotNull
                @jakarta.validation.constraints.Positive
                Long areaId,
        @io.swagger.v3.oas.annotations.media.Schema(description = "详细地址")
                @jakarta.validation.constraints.NotBlank
                @jakarta.validation.constraints.Size(max = 500)
                String detailAddress,
        @io.swagger.v3.oas.annotations.media.Schema(description = "是否默认地址")
                @jakarta.validation.constraints.NotNull
                Boolean defaultStatus)
        implements java.io.Serializable {}
