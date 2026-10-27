package com.lemnt.member.application.growth.cmd;

import java.io.Serializable;

/**
 * 签到配置删除命令。
 */
public record SignConfigDeleteCmd(
        Integer day
) implements Serializable {}
