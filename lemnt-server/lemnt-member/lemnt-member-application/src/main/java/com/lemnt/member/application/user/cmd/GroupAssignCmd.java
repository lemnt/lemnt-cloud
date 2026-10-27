package com.lemnt.member.application.user.cmd;

import java.io.Serializable;

/**
 * 分组分配命令。
 */
public record GroupAssignCmd(
        Long memberId,
        Long groupId
) implements Serializable {}
