package com.lemnt.member.adapter.web.account.req;

@io.swagger.v3.oas.annotations.media.Schema(description = "GroupSave请求")
public record GroupSaveReq(
        @io.swagger.v3.oas.annotations.media.Schema(description = "名称或收件人")
                @jakarta.validation.constraints.NotBlank
                @jakarta.validation.constraints.Size(max = 80)
                String name,
        @io.swagger.v3.oas.annotations.media.Schema(description = "备注")
                @jakarta.validation.constraints.Size(max = 500)
                String remark,
        @io.swagger.v3.oas.annotations.media.Schema(description = "状态：1启用，0停用")
                @jakarta.validation.constraints.NotNull
                Integer status)
        implements java.io.Serializable {}
