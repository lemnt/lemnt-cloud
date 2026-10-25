package com.lemnt.member.application.account.dto;

public record JsapiConfigDTO(String appId, String nonceStr, Long timestamp, String signature) {}
