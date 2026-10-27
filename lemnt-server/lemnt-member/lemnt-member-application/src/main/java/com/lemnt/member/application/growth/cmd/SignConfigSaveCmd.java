package com.lemnt.member.application.growth.cmd;

import java.io.Serializable;

/**
 * 签到配置保存命令。
 */
public record SignConfigSaveCmd(
        Integer day,
        Long point,
        Long experience,
        Integer status
) implements Serializable {}
