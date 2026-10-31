package com.lemnt.member.application.growth.cmd;

import java.io.Serializable;

/** 签到配置更新命令。 */
public record SignConfigUpdateCmd(
        Long id,
        Integer day,
        Long point,
        Long experience,
        Integer status
) implements Serializable {}
