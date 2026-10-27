package com.lemnt.member.application.growth.cmd;

import java.io.Serializable;

/**
 * 等级保存命令。
 */
public record LevelSaveCmd(
        Long id,
        String name,
        Integer level,
        Long experience,
        Integer discountPercent,
        String icon,
        String backgroundUrl,
        Integer status
) implements Serializable {}
