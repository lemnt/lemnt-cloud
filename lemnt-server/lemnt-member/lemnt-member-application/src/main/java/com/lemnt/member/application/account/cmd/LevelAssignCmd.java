package com.lemnt.member.application.account.cmd;

public record LevelAssignCmd(Long memberId, Long levelId, String requestNo, String reason) {}
