package com.lemnt.member.adapter.web.account.req;

@io.swagger.v3.oas.annotations.media.Schema(description = "TagSave请求")
public record TagSaveReq(
        @io.swagger.v3.oas.annotations.media.Schema(description = "名称或收件人")
                @jakarta.validation.constraints.NotBlank
                @jakarta.validation.constraints.Size(max = 80)
                String name,
        @io.swagger.v3.oas.annotations.media.Schema(description = "备注")
                @jakarta.validation.constraints.Size(max = 500)
                String remark)
        implements java.io.Serializable {}
