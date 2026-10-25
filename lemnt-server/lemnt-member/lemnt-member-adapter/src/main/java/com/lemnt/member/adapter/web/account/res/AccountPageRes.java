package com.lemnt.member.adapter.web.account.res;

@io.swagger.v3.oas.annotations.media.Schema(description = "会员分页响应")
public record AccountPageRes<T>(
        @io.swagger.v3.oas.annotations.media.Schema(description = "记录") java.util.List<T> records,
        @io.swagger.v3.oas.annotations.media.Schema(description = "总数") long total)
        implements java.io.Serializable {}
