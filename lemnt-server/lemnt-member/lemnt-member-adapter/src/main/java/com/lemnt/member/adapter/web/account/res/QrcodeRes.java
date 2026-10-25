package com.lemnt.member.adapter.web.account.res;

@io.swagger.v3.oas.annotations.media.Schema(description = "会员Qrcode响应")
public record QrcodeRes(
        @io.swagger.v3.oas.annotations.media.Schema(description = "base64") String base64,
        @io.swagger.v3.oas.annotations.media.Schema(description = "contentType") String contentType)
        implements java.io.Serializable {}
