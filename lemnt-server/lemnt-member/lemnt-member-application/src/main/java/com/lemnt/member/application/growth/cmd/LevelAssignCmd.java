package com.lemnt.member.application.growth.cmd;

import java.io.Serializable;

/**
 * 等级分配命令。
 */
public record LevelAssignCmd(
        Long memberId,
        Long levelId,
        String requestNo,
        String reason
) implements Serializable {}
