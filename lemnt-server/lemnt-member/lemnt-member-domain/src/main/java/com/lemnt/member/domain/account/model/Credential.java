package com.lemnt.member.domain.account.model;

public record Credential(Long memberId, String passwordHash) {}
