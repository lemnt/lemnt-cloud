package com.lemnt.member.domain.account.model;

public record MemberAdminFilter(
        Long memberId, String name, Integer status, String assetType, int offset, int limit) {}
