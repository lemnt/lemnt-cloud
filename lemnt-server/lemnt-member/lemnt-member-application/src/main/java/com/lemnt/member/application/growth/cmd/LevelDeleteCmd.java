package com.lemnt.member.application.growth.cmd;

import java.io.Serializable;

/**
 * 等级删除命令。
 */
public record LevelDeleteCmd(
        Long id
) implements Serializable {}
