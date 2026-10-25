package com.lemnt.member.adapter.web.account.req;

@io.swagger.v3.oas.annotations.media.Schema(description = "TagsAssign请求")
public record TagsAssignReq(
        @io.swagger.v3.oas.annotations.media.Schema(description = "标签编号，空集合清空标签")
                @jakarta.validation.constraints.NotNull
                @jakarta.validation.constraints.Size(max = 100)
                java.util.List<Long> tagIds)
        implements java.io.Serializable {}
