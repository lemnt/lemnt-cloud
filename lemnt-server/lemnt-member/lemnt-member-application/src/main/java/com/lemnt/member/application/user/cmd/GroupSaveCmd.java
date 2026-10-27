package com.lemnt.member.application.user.cmd;

import java.io.Serializable;

/**
 * 分组保存命令。
 */
public record GroupSaveCmd(
        Long id,
        String name,
        String remark,
        Integer status
) implements Serializable {}
