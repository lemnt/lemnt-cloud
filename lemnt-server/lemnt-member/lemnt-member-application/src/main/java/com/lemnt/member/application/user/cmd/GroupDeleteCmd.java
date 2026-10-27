package com.lemnt.member.application.user.cmd;

import java.io.Serializable;

/**
 * 分组删除命令。
 */
public record GroupDeleteCmd(
        Long id
) implements Serializable {}
