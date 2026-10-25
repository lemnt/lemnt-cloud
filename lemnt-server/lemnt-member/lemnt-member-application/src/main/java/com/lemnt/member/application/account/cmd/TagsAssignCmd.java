package com.lemnt.member.application.account.cmd;

public record TagsAssignCmd(Long memberId, java.util.List<Long> tagIds) {}
