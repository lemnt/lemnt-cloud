package com.lemnt.member.application.user.cmd;

import java.io.Serializable;

/**
 * 标签删除命令。
 */
public record TagDeleteCmd(
        Long id
) implements Serializable {}
