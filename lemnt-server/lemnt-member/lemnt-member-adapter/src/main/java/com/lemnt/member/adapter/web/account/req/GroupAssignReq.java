package com.lemnt.member.adapter.web.account.req;

@io.swagger.v3.oas.annotations.media.Schema(description = "GroupAssign请求")
public record GroupAssignReq(
        @io.swagger.v3.oas.annotations.media.Schema(description = "分组编号，可空") Long groupId)
        implements java.io.Serializable {}
