package com.lemnt.member.adapter.web.account.res;

@io.swagger.v3.oas.annotations.media.Schema(description = "会员Tag响应")
public record TagRes(
        @io.swagger.v3.oas.annotations.media.Schema(description = "记录编号") Long id,
        @io.swagger.v3.oas.annotations.media.Schema(description = "名称或收件人") String name,
        @io.swagger.v3.oas.annotations.media.Schema(description = "备注") String remark)
        implements java.io.Serializable {}
