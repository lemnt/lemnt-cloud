package com.lemnt.member.application.user.cmd;

import java.io.Serializable;
import java.util.List;

/**
 * 查询标签分配命令。
 */
public record TagsAssignCmd(
        Long memberId,
        List<Long> tagIds
) implements Serializable {}
