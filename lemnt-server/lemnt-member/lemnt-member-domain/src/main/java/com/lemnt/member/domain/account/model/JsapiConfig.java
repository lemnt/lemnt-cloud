package com.lemnt.member.domain.account.model;

public record JsapiConfig(String appId, String nonceStr, Long timestamp, String signature) {}
