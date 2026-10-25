package com.lemnt.member.adapter.web.account.res;

@io.swagger.v3.oas.annotations.media.Schema(description = "会员JsapiConfig响应")
public record JsapiConfigRes(
        @io.swagger.v3.oas.annotations.media.Schema(description = "appId") String appId,
        @io.swagger.v3.oas.annotations.media.Schema(description = "nonceStr") String nonceStr,
        @io.swagger.v3.oas.annotations.media.Schema(description = "timestamp") Long timestamp,
        @io.swagger.v3.oas.annotations.media.Schema(description = "signature") String signature)
        implements java.io.Serializable {}
