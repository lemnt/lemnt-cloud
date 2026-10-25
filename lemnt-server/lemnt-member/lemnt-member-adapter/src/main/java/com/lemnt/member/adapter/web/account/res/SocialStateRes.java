package com.lemnt.member.adapter.web.account.res;

@io.swagger.v3.oas.annotations.media.Schema(description = "会员SocialState响应")
public record SocialStateRes(
        @io.swagger.v3.oas.annotations.media.Schema(description = "state") String state,
        @io.swagger.v3.oas.annotations.media.Schema(description = "authorizationUrl")
                String authorizationUrl)
        implements java.io.Serializable {}
