package com.lemnt.member.adapter.web.account.res;

@io.swagger.v3.oas.annotations.media.Schema(description = "会员SubscribeTemplate响应")
public record SubscribeTemplateRes(
        @io.swagger.v3.oas.annotations.media.Schema(description = "id") String id,
        @io.swagger.v3.oas.annotations.media.Schema(description = "title") String title,
        @io.swagger.v3.oas.annotations.media.Schema(description = "content") String content,
        @io.swagger.v3.oas.annotations.media.Schema(description = "type") Integer type)
        implements java.io.Serializable {}
