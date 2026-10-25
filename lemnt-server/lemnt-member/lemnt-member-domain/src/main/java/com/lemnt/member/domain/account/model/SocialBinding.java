package com.lemnt.member.domain.account.model;

public record SocialBinding(String provider, String subject, Long memberId) {}
