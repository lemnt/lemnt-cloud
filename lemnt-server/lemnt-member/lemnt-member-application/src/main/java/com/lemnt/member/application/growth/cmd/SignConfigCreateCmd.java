package com.lemnt.member.application.growth.cmd;

import java.io.Serializable;

/** 签到配置新增命令。 */
public record SignConfigCreateCmd(
        Integer day,
        Long point,
        Long experience,
        Integer status
) implements Serializable {}
