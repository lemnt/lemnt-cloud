package com.lemnt.member.adapter.web.account.res;

@io.swagger.v3.oas.annotations.media.Schema(description = "会员Group响应")
public record GroupRes(
        @io.swagger.v3.oas.annotations.media.Schema(description = "记录编号") Long id,
        @io.swagger.v3.oas.annotations.media.Schema(description = "名称或收件人") String name,
        @io.swagger.v3.oas.annotations.media.Schema(description = "备注") String remark,
        @io.swagger.v3.oas.annotations.media.Schema(description = "状态：1启用，0停用") Integer status)
        implements java.io.Serializable {}
