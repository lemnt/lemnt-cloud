package com.lemnt.member.application.user.cmd;

import java.io.Serializable;

/**
 * 标签保存命令。
 */
public record TagSaveCmd(
        Long id,
        String name,
        String remark
) implements Serializable {}
